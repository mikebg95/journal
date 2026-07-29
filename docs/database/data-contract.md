# Phase 1 | Design the data

## Step 5: The data contract

### Tags (order matters)

1. NFC normalise → `Normalizer.normalize(input, Form.NFC)`
2. Trim start & end
3. Collapse inner whitespace runs (spaces/tabs) → single space
4. Lowercase
5. If empty now → REJECTED
6. If length > 50 → REJECTED
7. Uniqueness (no duplicates) → enforced by DB

### Title (order matters)

1. NFC normalise
2. Trim start & end (never touch internal formatting!)
3. If empty → REJECTED
4. If length > 100 → REJECTED

### Content (order matters)

1. NFC normalise
2. Trim start & end (never touch internal formatting!)
3. If empty → REJECTED
4. If length > 20000 → REJECTED

### Summary (order matters)

1. NFC normalise
2. Trim start & end
3. Collapse inner whitespace runs (spaces/tabs) → single space
4. If empty or null → ALLOWED (nullable)
5. If length > 500 → REJECTED

### Mood (order matters)

1. If empty → make null
2. If null → ALLOWED
3. NFC normalise
4. Trim start & end
5. Make uppercase
6. Match against 6 values (ENUM)
   1. If matches one → use it
   2. If doesn't match → null
7. (done — mood is either one of 6, or null)

### Todos (order matters) → for each todo:

1. NFC normalise
2. Trim start & end
3. Collapse inner whitespace runs (spaces/tabs) → single space
4. If empty → drop this todo from the list (do not store empty items)
5. If length > 1000 → REJECTED

The todo *list* itself may be empty (ALLOWED) — e.g. when the entry states no explicit todos.

---

### Nullability summary (drives the schema)

| Field | Nullable? |
|---|---|
| title | NOT NULL |
| content | NOT NULL |
| summary | nullable |
| mood | nullable |
| tags | list may be empty |
| todos | list may be empty |

**The principle:** user input (title, content) is required — empty is rejected. AI output (summary, mood, tags, todos) is optional — empty/null is allowed, because the AI may not have run (graceful degradation, ADR-0005).
