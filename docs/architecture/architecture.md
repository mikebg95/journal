# Architecture — P3 Notes & Journal

## The seven architecture levels

This document walks the seven architecture decisions for P3, comparing each against P1 and P2. For every level it states the decision, the reasoning, and the alternatives rejected. Of the seven levels, **two change from P2** (internal structure and data relationships) and **five stay the same** — and each change traces to a concrete pressure.

---

## Level 1 — Internal structure

**P1 & P2:** Layered architecture — simple CRUD apps with no external dependencies.

**P3:** Hexagonal architecture.

**What changed:** P3 introduces an external dependency — an LLM — that is slow, unreliable, hard to test, and likely to be swapped for another model in future. Hexagonal architecture (ports and adapters) fits this: the domain defines the **port** (an interface it owns), and the **adapter** implements it. For example, an OpenAI adapter implements the port; in tests it can be swapped for a fake that returns fast, free, reliable responses; and in future the OpenAI adapter can be replaced with an Anthropic or local-model adapter — all without touching the domain.

**Rejected:**

- **Layered** — In a layered architecture the LLM is called directly from the service, which means:
  - The AI library is baked into the business logic, so it can't be swapped for a fake. Testing against the real library is slow, costs money, and is unreliable (a different answer every time).
  - Swapping the AI library means editing business logic, because the library's types run through it. No decoupling.
- **Vertical Slice** — P1, P2 and P3 are too small and not feature-rich enough (only a few simple, similar features each) to benefit. In practice the result would be much the same, but vertical slice adds unnecessary complexity and code duplication, and reduces readability.

---

## Level 2 — Deployment granularity

**P1, P2 & P3:** Monolith — a single deployable unit.

**Why:** One developer, small applications, no pressure to scale (portfolio showcase apps with no expected growth in users or complexity).

**Rejected:**

- **Microservices** — Adds a large amount of unnecessary complexity, counter-productive for a one-person project. The application is too small and has no separate components that need to communicate or be deployed independently. Microservices suit large projects with multiple engineering teams that have different stacks, deadlines and goals, and applications whose components genuinely need to be independent.
- **Modular Monolith** — Sits between a regular monolith and microservices, and would make more sense than microservices. But P3 (and P1, P2) is too small and simple to need it — there is only one repository, one service and one controller. Modular monolith earns its place when a monolith grows large enough that internal module boundaries start to blur; P3 is nowhere near that.
- **SOA** — The older ancestor of microservices: largely legacy and superseded. Rejected for the same reasons as microservices, and additionally because it is outdated (microservices are finer-grained, decentralised, and the better choice in almost all cases).

---

## Level 3 — Hosting

**P1, P2 & P3:** Runs locally during development. Containerization (Docker), orchestration (Kubernetes) and cloud deployment (AWS) are deliberately out of scope for the backend project — they are separate stages in the roadmap (Stage C onwards). The application is built container-ready but is not yet containerized.

---

## Level 4 — Communication

*Request–Response (synchronous) vs Event-driven (asynchronous).*

**P1 & P2:** Request–Response (synchronous). Simple, fast database calls to an own database (milliseconds), no external dependencies. When something fails, it fails immediately, the problem is logged, and an error message/code is returned to the user. No need for asynchronous processing.

**P3:** Still Request–Response (synchronous) — but with an important change. P3 has an external dependency (the LLM) that is slow, unreliable and prone to failure. A deliberate decision is made to keep communication synchronous, despite this not being ideal UX (the user waits up to ~5 seconds after submitting an entry, which could look like the app has frozen). To offset this, the UI must make clear that the app has not crashed and that a call is in progress.

**Rejected:**

- **Event-driven (Asynchronous)** — Async is actually *better* UX: the entry saves instantly and the AI fields fill in a few seconds later. But it costs significant complexity: every new entry starts in an "unanalysed" state, requiring background jobs, a live "analysing…" status, a mechanism to update the UI when the AI finishes (polling or SSE), and handling for edits or failures that occur mid-analysis. For a single-user portfolio app this is not worth it. Synchronous is worse UX (a ~3s wait) but far simpler, and a clear "analysing your entry…" message makes the wait acceptable. Simplicity wins here.

---

## Level 5 — Read/Write model

**P1 & P2:** Single model — one model for both reading and writing. The default, and correct here: the object written to the database is the same object read from it.

**P3:** Also single model, for the same reason. P3's hardest read is "filter by tags," which is a simple database join — one model handles it fine.

**Rejected:**

- **CQRS** (splitting reads from writes) — Only useful when reads are so heavy, or so different from writes, that one shared model becomes a bottleneck. P3 has one user and simple queries, so there is no problem to solve.
- **Event Sourcing** (storing every change as a full history) — Only useful when a complete audit trail of how the data reached its current state is required — e.g. a banking app that must track every transaction. P3 does not need this.

---

## Level 6 — Domain model

*Where do the business rules live? Transaction Script vs Anemic Model vs DDD (rich domain model).*

**P1:** Subscription data is held in a Subscription object, so by definition not Transaction Script. Anemic vs DDD is technically undecided (there is no logic, so it lives neither in the object nor the service). Anemic by default, so P1 is concluded to be an Anemic Model.

**P2:** Recipe data is held in a Recipe object, so not Transaction Script. Logic mostly lives in service methods, but some lives in the domain object (e.g. re-numbering steps when a step is added or removed). So mostly Anemic, with some rich-domain aspects.

**P3:** Note data is held in a Note object, so not Transaction Script. Most logic lives in the service (AI call, find-or-create for tags, capping tag count, the save flow) — because it needs the database or the external AI, so it cannot live in an object. Some logic lives in the domain objects: `Tag.of()` cleans and validates itself (trim, lowercase, not blank), and Mood is a fixed enum. So mostly Anemic, with small rich touches — the same shape as P2.

**Rejected:**

- **Full DDD** — DDD earns its cost when the domain has rules that are costly to break and owned by a single object, and enough of them that rich objects prevent real bugs. P3 has essentially one such rule: a tag must always be clean, because a messy tag breaks the shared-tag/uniqueness system. That one rule is made rich (`Tag.of()`). Everything else is either trivial (the tag-count cap) or plumbing (AI, database) that cannot live in an object anyway. Not enough object-owned rules to justify full DDD → Anemic with rich touches.

---

## Level 7 — Data

*How is the data itself shaped?*

### SQL vs NoSQL

**P1 & P2:** SQL — relational, structured, connected data. Every object has the same structure (every subscription has an id, name and price; every recipe has an id, name, description, a list of ingredients and a list of steps; ingredients and steps have their own fixed structure). Ingredients and steps are linked to recipes via foreign keys.

**P3:** SQL, same as P1 & P2, for the same reason: every journal entry has the same structure (id, title, content, mood, tags), and tags have their own fixed structure, linked to entries via foreign keys. However, P3 differs from P2 here: P2 used one-to-many (steps/ingredients belong to one recipe), whereas P3 uses many-to-many (tags are shared across entries, via a join table). Still relational, still SQL — but the relationship shape changed.

### Shared database vs database-per-service

**P1, P2 & P3:** Not relevant. This only matters with microservices. Because P1, P2 and P3 are monoliths, they use a single database.

### Caching

**P1 & P2:** Not relevant — nothing worth caching, since all results are fast (internal service/database).

**P3:** Relevant. Results from the external LLM are slow and cost money, so caching lets similar results be reused, saving time and money. (Added in phase 3.5.)

### Consistency & the CAP theorem

*Strong vs eventual consistency.*

**P1, P2 & P3:** Strong consistency — the default.

**Rejected — Eventual consistency:** Not relevant in P1, P2 or P3. It only applies with multiple copies of the data (distributed nodes). With a single database, strong consistency comes for free.

---

## Summary

| Level | Decision | Changed from P2? |
|---|---|---|
| 1 — Internal structure | Hexagonal | **Changed** — driven by the LLM dependency |
| 2 — Deployment granularity | Monolith | No |
| 3 — Hosting | Local (cloud deferred) | No |
| 4 — Communication | Synchronous (deliberate compromise) | No |
| 5 — Read/Write model | Single model | No |
| 6 — Domain model | Anemic with rich touches | No |
| 7 — Data | Relational, many-to-many | **Changed** — shared tags |

Two levels change; five stay the same. Both changes trace to a concrete pressure — the LLM forces hexagonal, and shared tags force a many-to-many relationship.
