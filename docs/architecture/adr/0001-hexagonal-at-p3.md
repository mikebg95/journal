# ADR-0001: Hexagonal architecture at P3

**Status:** Accepted

## Context

An architectural decision has to be made for the internal structure of P3. The options are Layered architecture, Hexagonal architecture, or Vertical Slice architecture. P1 and P2 both used Layered architecture.

The key difference in P3 is a new external dependency: an LLM. It is slow, unreliable (it can fail, and returns different answers every time), and highly likely to be swapped for a different model in the future.

## Decision

P3 uses a **Hexagonal architecture** (ports and adapters).

The reason is the external LLM dependency. Three properties of it drive the decision:

- **It can't be reliably tested against the real model** — the model is slow, costs money per call, and returns different output each time. Isolating it behind a port means it can be replaced with a fake (stub) adapter in tests, which is fast, free and deterministic.
- **Its types would otherwise leak into the business logic** — in a layered structure the service would depend directly on the AI library, so swapping providers would mean editing business logic. A port keeps the library's types out of the core.
- **It is likely to be swapped** — the domain defines an LLM port, and the LLM adapter "plugs into" it. The domain is not aware of the adapter, so the adapter can be replaced with another (a different provider, or a local model) without changing the core.

The common thread: the domain owns an interface (the port), and the adapter implements it. This gives strict decoupling between the business logic and the external model.

## Alternatives rejected

- **Layered architecture** — In a layered structure the service layer (containing the business logic) would depend directly on the external LLM. That makes the business logic impossible to test without the real model (unreliable, non-deterministic), and welds the business logic to a specific AI library, so swapping providers means editing business logic. Hexagonal removes both problems by putting the model behind a port the domain owns.

- **Vertical Slice architecture** — Rejected because it would add unnecessary complexity with no matching benefit. Vertical Slice earns its place when there are many largely unrelated features; P3 has only a few small, similar features.

## Consequences

**What this buys:**

- The LLM can be faked in tests → fast, free, deterministic testing of the business logic.
- The LLM provider can be swapped without touching the core.
- The core has no framework or library dependencies — it is plain Java.

**What it costs:**

- More classes and indirection than layered — ports, adapters, and translation between the domain model and the persistence/DTO types. This is more boilerplate for a small application.
- A different structure and vocabulary from P1/P2: no "controller / service / repository" and no "layers". Instead, **ports and adapters**, split into driving and driven:
  - **Driving adapters** call *into* the domain. Here, the web adapter (`@RestController`) — the entry point, called from the frontend.
  - **Driven adapters** are called *out to* by the domain, by plugging into ports the domain owns. Here, the persistence adapter and the AI adapter.

## Revisit trigger

Reconsider this decision if the external LLM dependency is removed, or if the added structure proves to cost more than it saves — for example, if the model is never swapped and the isolation never provides real value. (Note: P4 deliberately returns to Layered, because it has no comparable external dependency — evidence that Hexagonal here is a response to a specific pressure, not a default.)
