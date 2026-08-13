# Field validation & enforcement

Where each rule is enforced: at the **request** boundary (Bean Validation), in the **domain** (cleaning logic), and in the **database** (constraints).

**Two request DTOs, not one.** *Create* carries only user input (title, content); the AI fields are filled server-side by the analysis. *Edit* carries user input **and** the AI fields, because the user can edit any of them (F10).

**User input** (title, content) is validated at the request on both DTOs *and* cleaned in the domain. **AI output** (summary, mood, tag, todo) is request-validated **only on the edit DTO** — where a human is sending it and a 400 is actionable — and is **never** request-validated on the create / analysis path, where the model fills it server-side. In the domain it is always cleaned gracefully (empty → null / dropped, over-length → null / dropped, invalid → dropped), never rejected. All fields are backed by DB constraints as a final safety net.

The rule of thumb: **reject when a human is watching, clean when the model is.** Same field, different entry point, different treatment.

| Field | Rules | Enforced in request | Enforced in domain | Enforced in DB |
|---|---|---|---|---|
| **title** | not null/blank/empty; ≤ 100 | `@NotBlank`, `@Size(100)` (both DTOs) | Trim whitespace (outer); NFC normalisation | `NOT NULL`, `VARCHAR(100)` |
| **content** | not null/blank/empty; ≤ 20000 | `@NotBlank`, `@Size(20000)` (both DTOs) | Trim whitespace (outer); NFC normalisation | `NOT NULL`, `TEXT`, `CHECK (length(content) <= 20000)` |
| **summary** | over-length rejected on edit; empty → null; null allowed; ≤ 500 | Edit DTO: `@Size(max = 500)`. Not on create DTO. | Trim (outer); collapse inner whitespace runs; NFC; if empty/blank → null; if length > 500 → null | `VARCHAR(500)`, nullable |
| **mood** | not blank/empty → becomes null; null allowed; enum check (one of six values or null) | — | Trim (outer); collapse inner whitespace runs; NFC; uppercase; if empty/blank → null; check enum allowed (else null) | `VARCHAR(20)`, `CHECK (mood IN ('HAPPY','CALM',...))`, nullable |
| **tag** | over-length rejected on edit; empty → dropped; ≤ 50; unique | Edit DTO: `@Size(max = 50)` per item. Not on create DTO. | Trim (outer); collapse inner whitespace runs; NFC; lowercase; if empty/blank/null → remove; if length > 50 → remove | `NOT NULL`, `VARCHAR(50)`, `UNIQUE` |
| **todo** | over-length rejected on edit; empty → dropped; ≤ 1000 | Edit DTO: `@Size(max = 1000)` per item. Not on create DTO. | Trim (outer); collapse inner whitespace runs; NFC; if length > 1000 → remove; if empty/blank/null → remove | `NOT NULL`, `VARCHAR(1000)` |
| **list of tags** | can be empty (length 0); no duplicates; max length 10 | Edit DTO: `@Size(max = 10)`. Not on create DTO. | Remove null values; remove empty strings; remove duplicates (by canonical value); **then** check max length (10) → remove excess | — |
| **list of todos** | can be empty (length 0); no exact duplicates; max length 20 | Edit DTO: `@Size(max = 20)`. Not on create DTO. | Remove null values; remove empty strings; remove exact duplicates (keep the first occurrence); **then** check max length (20) → remove excess | — |

**Notes**

- The `tag` UNIQUE constraint works on the stored value because tags are already lowercased in the domain before saving — so "Work" and "work" collide correctly.
- Lists (tags, todos) are not single DB columns; each item is its own row. The list-level rules (max count, dedupe, remove empties) are enforced in the domain/service, not the DB — over-count is rejected on the edit path (`@Size`) and trimmed on the analysis path.
- **Duplicate todos are removed, and only in the domain.** Two todos whose cleaned text is identical *within one entry* are the model repeating itself, not two tasks — the same family of rule as dropping blanks, not an identity claim. The first occurrence is kept so the order survives. Unlike tags there is no DB constraint: todos are owned by a single entry and written through one path, and a duplicate is cosmetic rather than corrupting. Tags get an index as well because they are shared and found-or-created concurrently, where a race can produce a duplicate the code never saw.
- **Deduplicate before capping — both lists.** Capping first lets duplicates consume slots and silently discards good items: twelve tags containing two duplicates would yield eight, not ten. The loss is invisible, which is what makes the ordering worth writing down.
- **Edit path vs analysis path.** When a user edits an AI field, over-length is rejected at the request (edit DTO `@Size`), so the domain never sees it. Everything else the user can legitimately produce — clearing the summary (empty → null), removing every tag, an out-of-set mood via the raw API (→ null) — is *not* a request error; it passes through and the domain cleans it.
- **Why the domain still handles over-length.** On the analysis path there is no request layer, so the domain's over-length cleaning (summary → null, tag/todo → dropped) is what stops an over-length model value from reaching the DB. Relying on the `VARCHAR` constraint alone would throw and fail the whole save instead of degrading — the opposite of R1/R2.
