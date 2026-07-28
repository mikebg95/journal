# Requirements — P3 Notes & Journal

## Functional requirements

- **Create** — User can create a new journal entry, inserting a title and content (both required).

- **Analyse (one AI call)** — In a single AI call, the app analyses the journal entry and produces:
  - a one-sentence summary;
  - relevant tags — an entry can have one or more;
  - a mood — an entry has exactly one;
  - a list of todos based on what is *explicitly stated* in the entry; if there are none, an empty list.

- **Save** — The app saves the journal entry and its related data (title, content, summary, tags, mood, todos), plus a created timestamp and a last-updated timestamp, to the database.

- **Shared tags** — Tags are shared across entries: the same tag on two entries is the same tag.

- **View list** — User can view a list of journal entries (title, date, mood, last-updated, tags), sorted by last-updated (newest on top). The list is paginated — entries are returned in fixed-size pages, not all at once.

- **Filter by tags** — User can filter entries by tag(s). Tag filtering is multiselect with **AND** semantics: selecting multiple tags narrows the results, showing only entries that have *all* the selected tags. No tags selected means no tag filtering.

- **Filter by moods** — User can filter entries by mood(s). Mood filtering is multiselect with **OR** semantics: selecting multiple moods widens the results, showing entries that match *any* of the selected moods. No moods selected means no mood filtering.

- **Edit** — User can edit previous entries (title, content) as well as the AI-generated fields (tags, todos, mood, summary).

- **Re-generate** — User can manually re-generate the AI-generated fields (tags, todos, mood, summary) with a dedicated button. Usable after editing an entry, as a retry if the app fails, or simply when the user wants different AI results.

- **Delete** — User can delete a previous entry, including all its related content (title, content, tag associations, todos, mood, summary, etc.).

- **View details** — User can click a journal entry and see its details.

---

## Non-functional requirements

### Scalability
Does not have to scale. It is a portfolio project with currently one user. New users are one-time visitors viewing the application on the portfolio.

### Availability
Does not have to be high. Cold start is acceptable, even for a recruiter — a few seconds of wait is fine. Paying to keep it always-on is not worth it for a portfolio project.

### Performance / Latency
- Fast actions (list, view, filter, edit, delete) should take less than 1 second.
- Slower actions (AI calls) take ~2.7s on average (range 1.9–4.9s) and must finish within 5 seconds. The user waits for the result on purpose (synchronous); see ADR-0002.

### Reliability
- The AI produces a summary, tags, a mood, and todos. If the AI call fails, the entry is still saved (without AI results); the user can re-generate later.
- The system must limit the tag count. The system must handle empty results from the AI — e.g. set string(s) to null or set an empty list.

### Security
Out of scope for this project. It is a simple portfolio project to showcase Spring AI. No Spring Security, no authentication, no authorization.

### Maintainability
High priority. The main "user" is a senior dev or employer reading the code. Clean architecture, clear code, and documented decisions are the actual product.

### Observability
When something goes wrong, it should be clear what happened. The user gets a general error message ("something went wrong"); more specific error details are logged to the system.

### Cost
Should be very cheap — no real users, just a portfolio showcase. Preferably free.

### Testability
The AI is slow and unpredictable and can't be reliably tested against the real model. It must sit behind an interface that can be replaced with a fake version in tests. This keeps tests fast, free, and reliable.

---

## Measured baseline (day-one spike)

| Measurement | Value |
|---|---|
| AI latency (average) | ~2.7s |
| AI latency (range) | 1.9–4.9s |
| Structured output binding | Hard — valid JSON returned, both providers |
| Tag count stability | Unstable (2–4 tags for the same input) → cap in code |
