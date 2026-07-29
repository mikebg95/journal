# ADR-0004: No cascade delete to shared tags

**Status:** Accepted

## Context

A decision has to be made about what happens to an entry's tags when that entry is deleted. Because tags are **shared** across entries (a many-to-many relationship), the same tag can be linked to many entries at once. So the question is whether deleting an entry should also delete its tags, or only remove that entry's links to them.

## Decision

Deleting an entry does **not** delete its tags.

When an entry is deleted, the application removes that entry's rows in the join table (`entries_tags`) — unlinking it from its tags — but never deletes rows in the `tags` table itself. A tag survives as long as any entry still references it.

The reason is the many-to-many relationship: a tag is typically linked to several entries. If deleting one entry cascaded into deleting its tags, every *other* entry linked to those same tags would lose them too — silently corrupting unrelated entries. Removing only the associations keeps the deleted entry's cleanup local: no other entry is affected.

## Alternatives rejected

* **Cascade delete to tags** — Deleting an entry would also delete its tag rows. Rejected because, with shared tags, this removes those tags from every other entry that uses them. Deleting one entry would damage unrelated entries. This is only safe in a one-to-many model (where each tag belongs to a single entry), which is not the case here.

* **A cleanup job for orphaned tags** — A background job could periodically delete tags that are no longer linked to any entry. Considered and deferred: it adds scheduling and complexity for no real benefit in a single-user portfolio app. Orphaned tags are harmless (see consequences). Out of scope for P3.

## Consequences

**What this buys:**

- No data loss when deleting an entry. Other entries keep their tags intact.
- Entry deletion is simple and local — it only touches that entry's own rows and its join rows.

**What it costs:**

- Orphaned tags — tags no longer linked to any entry — can accumulate in the `tags` table over time. This is harmless: they take negligible space and do not affect correctness or behaviour. Cleaning them up is optional future work, deliberately left out of P3.

## Revisit trigger

Reconsider this decision if the entry–tag relationship changes from many-to-many to one-to-many — i.e. if each tag belonged to exactly one entry. In that case a tag could not be shared, so cascade-deleting a tag along with its entry would no longer risk affecting any other entry, and cascading would become the correct choice.
