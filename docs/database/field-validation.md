# Step 6: Field validation & enforcement

Where each rule is enforced: at the **request** boundary (Bean Validation), in the **domain** (cleaning logic), and in the **database** (constraints).

**User input** (title, content) is validated at the request *and* cleaned in the domain. **AI output** (summary, mood, tag, todo) has no request validation — it is cleaned gracefully in the domain (empty → null, invalid → dropped) — because it comes from the model, not the user. All fields are backed by DB constraints as a final safety net.

| Field | Rules | Enforced in request | Enforced in domain | Enforced in DB |
|---|---|---|---|---|
| **title** | not null/blank/empty; ≤ 100 | `@NotBlank`, `@Size(100)` | Trim whitespace (outer); NFC normalisation | `NOT NULL`, `VARCHAR(100)` |
| **content** | not null/blank/empty; ≤ 20000 | `@NotBlank`, `@Size(20000)` | Trim whitespace (outer); NFC normalisation | `NOT NULL`, `TEXT`, `CHECK (length(content) <= 20000)` |
| **summary** | not blank/empty → becomes null; null allowed; ≤ 500 | — | Trim (outer); collapse inner whitespace runs; NFC; if empty/blank → null | `VARCHAR(500)`, nullable |
| **mood** | not blank/empty → becomes null; null allowed; enum check (one of six values or null) | — | Trim (outer); collapse inner whitespace runs; NFC; uppercase; if empty/blank → null; check enum allowed (else null) | `VARCHAR(20)`, `CHECK (mood IN ('HAPPY','CALM',...))`, nullable |
| **tag** | not null/blank/empty; ≤ 50; unique | — | Trim (outer); collapse inner whitespace runs; NFC; lowercase; if empty/blank/null → remove | `NOT NULL`, `VARCHAR(50)`, `UNIQUE` |
| **todo** | not null/blank/empty; ≤ 1000 | — | Trim (outer); collapse inner whitespace runs; NFC; if length > 1000 → remove; if empty/blank/null → remove | `NOT NULL`, `VARCHAR(1000)` |
| **list of tags** | can be empty (length 0); max length 10 | — | Remove null values; remove empty strings; check max length (10) → remove excess | — |
| **list of todos** | can be empty (length 0); max length 20 | — | Remove null values; remove empty strings; check max length (20) → remove excess | — |

**Notes**

- The `tag` UNIQUE constraint works on the stored value because tags are already lowercased in the domain before saving — so "Work" and "work" collide correctly.
- Lists (tags, todos) are not single DB columns; each item is its own row. The list-level rules (max count, dedupe, remove empties) are enforced in the domain/service, not the DB.
- When a user *edits* an AI field directly, it passes through the same domain cleaning; the graceful rules (empty → null, invalid → dropped) still apply.
