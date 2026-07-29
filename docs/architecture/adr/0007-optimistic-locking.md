# ADR-0007 — Optimistic locking on entries, with user-resolved conflicts

## Status

Accepted.

## Context

An entry can be read and written in two separate steps, with an arbitrary gap between them. In that gap, another write can land. The second write then overwrites the first, based on data that is already stale. Nothing crashes and nothing is logged — the change simply disappears. This is a **lost update**.

The concrete scenario:

1. The user opens an entry to edit. The form loads the entry at version `1`. The user starts making changes but does not save.
2. In a second tab, the user opens the same entry, makes different changes, and saves. The stored entry is now version `2`.
3. The user returns to the first tab and saves.

Without protection, step 3 writes the entry as it looked in step 1 and the changes made in step 2 are gone. The user would only discover this by rereading the entry later and noticing something missing.

**This is reachable with a single user.** No traffic and no second person are required:

- Two tabs open on the same entry.
- Save pressed twice, or a network-level retry.
- The browser Back button restoring a cached edit form holding a stale version.
- A re-analysis running while the user edits the same entry — a 2–10 second window created by the application's own design.

**The widest window is not the AI call.** The AI call is 2–10 seconds. The gap between loading the edit form and pressing save is however long the user leaves the tab open — potentially hours. The dominant risk is a human pausing, not a model responding slowly.

The question this record answers is not only *how to detect* the conflict, but *who decides* the outcome.

## Decision

**Entries carry a version. A write based on a stale version is rejected rather than applied. The user chooses how to resolve it.**

### Detection

A `version` column is added to `entries` and managed by the persistence provider via `@Version`. The application never assigns or increments it. The value starts at `0` on insert and increases by one on every update.

The check and the write are a **single atomic statement**:

```sql
UPDATE entries SET title = ?, content = ?, ..., version = 2
WHERE id = 47 AND version = 1
```

If another writer already moved the version, zero rows match, the provider raises an optimistic-lock failure, and the global exception handler translates it to `409 Conflict`. The stored entry is not modified.

**The version is never read and compared in application code.** Fetching the current version, comparing it in Java, and then writing reintroduces exactly the gap this record exists to close — the comparison would be true when made and false when used. Correctness depends on the check and the write being indivisible, which only the database can guarantee.

### The API contract

The client sends the version it read. The `409` response carries the current stored version in the standard error body, so the client does not need an extra round trip to recover:

```json
{ "type": "…/conflict", "title": "Entry was modified", "status": 409, "currentVersion": 2 }
```

### Resolution — asymmetric by writer

**A user editing** gets a choice. On `409` the interface shows:

> **This entry changed**
> Someone saved a newer version. Showing the updated entry discards your changes.
> `Show updated entry` · `Keep my changes`

- *Show updated entry* — local changes are discarded and the entry is re-fetched.
- *Keep my changes* — the client resubmits the user's text with the version from the `409` response. The previously stored changes are overwritten.

**A re-analysis** gets no dialog. If an analysis completes and the entry has moved on, the result is discarded silently and the *Analyse again* action remains available. The summary describes text that no longer exists, so it is worthless, and there is no user present to ask. Discarding is more correct than either overwriting the user's edit or interrupting them.

**Deletion does not require a version.** Deleting an entry someone else just edited still produces the intended outcome. The version guards content, not existence.

## Alternatives rejected

**No versioning.** The step-3 save succeeds and the step-2 changes vanish with no error, no log line, and no way to detect it afterwards. Silent, irreversible loss of the user's own writing. This is the alternative that matters most, and it is the default outcome of doing nothing.

**Versioning with no conflict handling.** Adding the column and letting the exception escape produces a `500` and "something went wrong." Better than silent loss — nothing is destroyed — but the user cannot act on it. This is what the system degrades to if the handler is missing, which is why the `409` mapping is part of the decision rather than an implementation detail.

**Always discard the user's unsaved changes** (reload-only, no second button). Nothing stored is ever overwritten, which is genuinely safer, and this was the closest call. Rejected because in a journal both versions are the user's own prose: whichever side is discarded, writing is lost. Making that choice on the user's behalf, silently, is the thing this record is trying to avoid. Retained as the fallback if overwriting is ever judged unacceptable — it is one button removed.

**Always overwrite the stored entry with the unsaved changes.** Removes the dialog, but destroys the other change without telling anyone. That is the original bug with a version column bolted on.

**Pessimistic locking** — lock the row on read and hold it until the write. It works, and it is the wrong mechanism here: the lock must be held inside a transaction, and this application's write path has a 2–10 second network call in the middle. A pessimistic lock would hold both a row lock and a database connection open for that entire call. With a default pool of ten connections, a handful of concurrent saves exhausts the pool and every request in the application fails — including ones unrelated to entries — while the symptom points at the database and the cause is the AI provider. It is the transaction trap in a different costume.

**Automatic merge.** Apply both sets of changes where they touch different parts of the entry. The best outcome and by far the largest: it requires field- or line-level diffing, and "set the content to X" is not mergeable in the general case. This is what collaborative editors implement, and it is a feature, not a dialog.

**`If-Match` / `ETag` headers with `412 Precondition Failed`.** The more correct HTTP expression of the same idea, and defensible. Rejected in favour of carrying the version in the request body, which is simpler for a single client and keeps conflict handling uniform with the rest of the error format. Worth noting that resource versioning is not an ORM concept leaking into the API — conditional requests predate ORMs, which is why `ETag` exists.

## Consequences

**What it buys**

- Silent data loss becomes an explicit, visible decision. This is the primary benefit; the improved experience is a consequence of it, not the point.
- The conflict is caught by the database, so the protection holds across multiple application instances. Application-level coordination would not.
- The mechanism is nearly free when there is no conflict: no locks, no waiting, no held connections.
- It composes with the transaction boundaries already required by the synchronous AI call, rather than fighting them.

**What it costs**

- **`Keep my changes` can overwrite another change, deliberately.** The guarantee is not that no data is ever lost — it is that no data is lost *without someone choosing it*. That distinction is the whole justification for the second button, and it should be stated rather than glossed over.
- The version enters the public API contract, in both requests and responses. This record therefore has to precede the API specification, not follow it.
- The client must hold the version it read, send it back, and handle a `409` with a two-option dialog.
- **Resolution can conflict again.** If a third write lands between the `409` and the resubmit, the resubmit is also rejected. Correct behaviour, same dialog, same options — but it must not be retried automatically in a loop.
- Any collection change on the entry bumps its version by default, so editing tags or to-dos counts as changing the entry. Correct here, because analysis writes content, summary, mood, tags and to-dos as one unit of change.

**Two ways this silently stops working**

Both compile, pass review, and fail nothing:

1. **The mapper resets the version.** Converting a domain object into a *new* entity and saving it on update discards the counter. The conflict detection then never fires. The persistence adapter must load the managed entity and update it in place, excluding `id`, `version`, and `created_at`.
2. **The server supplies its own version.** Fetch the entry, apply the changes, save — the version always matches, so the update always succeeds and the column does nothing. The one legitimate exception is the resubmit after a `409`, where the user has been shown the conflict and chosen to overwrite, and the re-analysis path, where the version read at the start is guarding a real gap. Both look identical to the mistake, which is why they are named here.

Because both failures are invisible, the test is not optional: create an entry, update it once so the version moves, submit using the stale version, and assert the write is rejected and the good text survived. It reproduces the *state*, not the timing, so it needs no threads and no sleeps and is fully deterministic.

## Revisit trigger

**A second automated writer is introduced** — for example a scheduled sweep that re-analyses entries whose analysis failed.

A background writer produces conflicts with no human present to resolve them, so a dialog stops being a sufficient strategy and the system needs an automatic policy per writer. The same applies if the application gains real concurrent users, at which point merge behaviour becomes worth its cost.