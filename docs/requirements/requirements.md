# Requirements — P3 Journal

## Functional requirements

| # | Requirement |
|---|---|
| F1 | **Create** — User can create a journal entry with a title and content. Both are required. |
| F2 | **Analyse** — In a single AI call, the app produces a summary, tags, a mood, and to-dos. |
| F3 | **Save** — The app stores the entry, its analysis, and its timestamps. |
| F4 | **Shared tags** — The same tag on two entries is the same tag. |
| F5 | **View list** — Paginated, sorted by last-updated, newest first. |
| F6 | **Filter by tags** — Multiselect, AND semantics. |
| F7 | **Filter by moods** — Multiselect, OR semantics. |
| F8 | **Search** — Free-text over title and content. |
| F9 | **View details** — Full entry and its analysis. |
| F10 | **Edit** — Title, content, and every AI-generated field. |
| F11 | **Re-generate** — Manually re-run the analysis. |
| F12 | **Delete** — Remove an entry and everything belonging to it. |
| F13 | **View all to-dos** — Every to-do from every entry in one list, each linking to its source entry. |

### F1 — Create

Title and content are both required; neither may be blank. Limits are `title` 100 characters and `content` 20 000 characters, enforced in the application and in the database.

### F2 — Analyse

One AI call per analysis, producing four things:

| Output | Cardinality | Notes |
|---|---|---|
| Summary | zero or one | One sentence. Maximum 500 characters. |
| Tags | zero or more | Capped at **10** in application code. The prompt requests a limit; the code enforces it. |
| Mood | zero or one | Exactly one of six fixed values, or none. |
| To-dos | zero or more | Only what the entry *explicitly states*. No invented tasks. Each maximum 1 000 characters. |

Every output is optional. The AI may fail entirely, or succeed and return nothing useful for a given field — a two-word entry legitimately yields no tags and no to-dos. The system treats an empty result as valid, not as an error.

The six moods are `HAPPY`, `CALM`, `NEUTRAL`, `ANXIOUS`, `SAD`, `FRUSTRATED`. A value the model returns that matches none of them becomes null rather than an error.

### F3 — Save

Persists the entry, its analysis, a created timestamp, a last-updated timestamp, and an **analysed-at timestamp**.

`analysed_at` is null until an analysis succeeds. Comparing it against `last_updated` is what lets the system distinguish *never analysed* from *analysis is out of date*, which is otherwise not derivable.

### F4 — Shared tags

Tags are stored in their canonical form: NFC-normalised, trimmed, inner whitespace collapsed, lowercased. Uniqueness applies to that canonical value, so `Work`, `work` and ` WORK ` are one tag.

Canonicalisation happens once, in the domain, and is additionally enforced by database constraints. This matters more here than in a human-typed application, because the model produces inconsistent casing and spacing for the same concept across runs.

### F5 — View list

Fixed page size of 20. Sorted by last-updated descending, not user-sortable.

Each row shows: title, truncated summary, mood, first two tags alphabetically plus an overflow count, a to-do count, and the last-updated date.

Entry **content is deliberately not shown** in the list. Prose does not tabulate, and every other column is analysis output — which makes the list itself the demonstration of the feature.

### F6 / F7 — Filtering

Tags use AND, moods use OR. The asymmetry follows from cardinality rather than preference: an entry has many tags, so requiring all of them is the useful operation; an entry has one mood, so requiring several would always return nothing.

No tags selected means no tag filtering. No moods selected means no mood filtering. Filters combine with each other and with search.

### F8 — Search

Case-insensitive substring match over `title` and `content`. Combines with both filters.

Not full-text search. Stemming requires committing to a language dictionary, and entries are expected in mixed languages — the summary is generated in the entry's own language. A substring match is language-neutral.

### F10 — Edit

Every field is editable, including the analysis. Any edit updates `last_updated`, which marks the existing analysis out of date until re-generated.

Concurrent edits are detected, not prevented — see R4.

### F11 — Re-generate

Available after editing, as a retry when analysis failed, or on demand for a different result.

**A failed re-generation must leave the previous analysis intact.** Nothing is cleared in anticipation of a result.

Re-generation replaces the analysis wholesale, including the to-do list. This is safe only because to-dos carry no user state — see the non-goals.

### F12 — Delete

Removes the entry, its content, its to-dos, and its tag associations.

**Tags themselves are never deleted.** They are shared, so deleting an entry's tags would destroy them for every other entry using them. A tag left with no entries is accepted and harmless.

### F13 — View all to-dos

Read-only. Every to-do across all entries, showing the to-do text, its source entry title as a link, that entry's mood, and its date. Sortable newest or oldest first. Paginated at 20.

Read-only in this project. Completion, removal and reordering would place user state into a list that re-generation replaces.

---

## Non-functional requirements

Read as a difference from P2. Four moved, and all four have the same cause.

| Quality | P2 | P3 | Changed |
|---|---|---|---|
| Scalability | One user | One user | — |
| Availability | Nobody cares | Nobody cares | — |
| Security | No login | No login | — |
| Maintainability | High | High | — |
| **Latency** | Milliseconds | AI call ~2.7s | **new** |
| **Reliability** | Database is reliable | AI can fail, stall, or return nothing | **new** |
| **Cost** | Effectively nothing | Every call costs money | **new** |
| **Testability** | Trivial | Non-deterministic dependency | **new** |

### Scalability
Does not have to scale. One real user; visitors are one-time viewers of a portfolio.

### Availability
Does not have to be high. Cold start is acceptable, including for a recruiter. Paying to keep it warm is not worth it.

### Performance
- Fast actions — list, view, filter, search, edit, delete — under 1 second.
- The analysis call is synchronous and the user waits, deliberately (ADR-0002).
- Measured: ~2.7s average, 1.9–4.9s observed range.
- **User-facing target: under 5 seconds.** This is a UX expectation, not a timeout.
- **Technical timeout: 15 seconds**, with a bounded total across retries. A timeout set at the measured worst case would fail healthy-but-slow calls; the Guide's rule is to set the limit meaningfully above the observed worst case.
- **Revisit trigger for ADR-0002: the slowest 5% of saves exceeding 3 seconds.** This requires the latency metric in the observability section to exist — a numeric trigger that cannot be measured is decoration.

### Reliability
- **R1 — Graceful degradation.** If analysis fails, the entry is still saved without its analysis and the request still succeeds. Losing a user's writing because an analysis service was unavailable is not acceptable. The user can re-generate later.
- **R2 — Empty results are valid.** Null summary, null mood, empty tag list, empty to-do list are all legitimate states, not errors.
- **R3 — The tag count is capped in code.** The prompt asks; the code enforces. A prompt is a request, not a guarantee.
- **R4 — Concurrent modification is detected.** Each entry carries a version. A write based on a stale version is rejected with a conflict response rather than silently overwriting. This exists because reading an entry, waiting for an analysis, and writing it back is a wide window — and the widest window in the application is a user sitting on an open edit form.
- **R5 — Only transient failures are retried.** Timeouts and rate limits are retried with backoff; invalid credentials and malformed requests are not. Total retry time is budgeted so retries cannot exceed the request's overall deadline.
- **R6 — Re-analysis is idempotent.** Running it twice produces the same result as running it once. Tags are reused rather than duplicated, and the analysis is replaced rather than appended.

### Security
Out of scope. No authentication, no authorization. The one hard rule: the API key is supplied by environment variable and never enters version control, and its absence is handled rather than crashed on.

### Maintainability
High priority. The real audience is a senior engineer reading the repository. Clean boundaries, decision records, and enforced architecture rules are the actual product.

### Observability
- The user sees a general message; specifics go to the logs.
- **Structured logging**, with correlation ids so one request's story can be read end to end.
- **Never log entry content.** Journal entries are extremely personal. Log the identifier and the length.
- **Degradation logs at warning, not error.** The system handled it; alerting on correct behaviour teaches you to ignore alerts.
- **Metrics required:** analysis latency including the slow tail, outcome, token usage, retry count, and a counter for entries saved without analysis. That last one catches exactly what the logs deliberately downplay — a system where 30% of entries save unanalysed is technically working and practically broken.

### Cost
Should be near free. No real users. Tests never call the real model; exactly one live test does, and it is excluded from the normal build and skipped when no key is present. Caching identical analyses is a later project.

### Testability
The model is slow, priced, and returns a different answer every time, so there is nothing stable to assert. It sits behind an interface the application owns, replaceable with a hand-written fake. This is the decisive argument for the architecture, not swappability.

Fakes prove the logic; one live test proves the integration. The live test asserts structure only — never content.

### Accessibility
- Text contrast meets WCAG AA. Filled-button labels are never smaller than 14px/500, where the brand and danger fills clear AA for large text.
- Colour is never the only signal; status is colour plus text.
- Keyboard focus is always visible; reduced motion is respected.
- Light and dark themes both supported, following the operating system with a manual override.

---

## Measured baseline (day-one spike)

| Measurement | Value |
|---|---|
| AI latency, average | ~2.7s |
| AI latency, range | 1.9–4.9s |
| Structured output binding | Hard — valid JSON returned, both providers |
| Tag count stability | Unstable: 2–4 tags for identical input → cap in code |

---

## Non-goals

Recorded so they read as decisions rather than omissions.

| Not building | Why |
|---|---|
| Authentication, accounts, sharing | P4 and later |
| Background processing of the analysis | Correct for a real product; deferred under one-new-thing-at-a-time (ADR-0002) |
| Streaming the summary | Requires the response to stop being one finished object |
| To-do completion | Re-generation replaces the to-do list, so a completion flag would need an unreliable text-match merge to survive |
| Manual to-do reordering | Order is insertion order; a position column buys nothing for a handful of items and would let a cosmetic drag cause a conflict |
| Editing to-dos from the aggregate list | Would bump a parent entry's version from a page with no conflict handling |
| Sortable list columns | Newest-first is the only ordering a journal needs |
| Bulk operations, undo after delete | No requirement; multiplies the destructive surface |
| Tag management screen | Tags are generated, not curated |
| Full-text search | Language mismatch, not scope — see F8 |
| Retrieval, embeddings, agents, tool calling | Explicitly a later project |
| Circuit breaker | Conditional, not unnecessary: no traffic, no shared resource held across the call, cheap workers. Change any one and it becomes required. |
