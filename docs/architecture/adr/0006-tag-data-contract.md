# ADR-0006: Tag normalisation defines tag identity

**Status:** Accepted

## Context

Tags are **shared** across entries (many-to-many), and they come from **two sources**: the user (typed directly, or edited) and the AI (generated during analysis). The same tag can arrive written in many ways — "Work", "work", " work ", "WORK", or with different Unicode encodings of the same characters.

Without a consistent rule for what makes two tags "the same", these variants would be stored as separate tags. That would break the shared-tag system (the same concept split across many rows) and make the unique constraint meaningless. A single, well-defined normalisation contract is needed to decide tag **identity**.

## Decision

Every tag is normalised before it is stored or compared, using one fixed sequence of rules:

1. NFC Unicode normalisation
2. Trim outer whitespace
3. Collapse inner whitespace runs to a single space
4. Lowercase
5. Reject if empty after cleaning
6. Reject if longer than 50 characters

The **normalised form is the tag's identity** — it is what uniqueness and matching are based on. The database enforces this with a `UNIQUE` constraint on the stored (already-normalised) value.

Crucially, this cleaning lives in **one place** — the `Tag.of()` factory in the domain — so that user-entered and AI-generated tags pass through the *identical* rules. Because tags have two sources, the rules cannot live at the request boundary alone; they must live in the object itself, so every path to a tag is normalised the same way.

## Alternatives rejected

* **Validate/clean tags only at the API request.** Rejected because AI-generated tags never pass through a request — they arrive from the model inside the backend. Request-only cleaning would leave AI tags un-normalised, producing duplicates. The rules must live in the domain, where both sources converge.

* **Store tags as the user/AI wrote them, and compare case-insensitively at query time.** Rejected because it pushes the normalisation into every query, is easy to apply inconsistently, and cannot be enforced by a simple `UNIQUE` index. Normalising once, on write, is simpler and safer.

## Consequences

**What this buys:**

- "Work", "work", and " work " are correctly treated as one shared tag.
- The unique constraint is meaningful and enforced by the database, because it operates on an already-normalised value.
- Both sources of tags (user and AI) are guaranteed to be consistent, because they share one cleaning path.

**What it costs:**

- The normalisation rules become **baked into the stored data and the unique index**. This is the one part of the data design that cannot be changed cheaply later: altering the rules (e.g. deciding "café" and "cafe" should now match) would require migrating and de-duplicating every existing tag. The contract must therefore be decided carefully up front.

## Revisit trigger

Reconsider if the identity rules themselves need to change — for example, if accent-folding ("café" = "cafe") becomes desired, or if tags should preserve their original display case. Any such change is a data migration, not just a code change, and must be treated as one.
