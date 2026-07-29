# ADR-0005: Graceful degradation on AI failure

**Status:** Accepted

## Context

The application calls an external LLM to generate information (summary, tags, mood, todos) about a journal entry, based on the title and content the user enters. That generated information is then stored.

The LLM is an external, unreliable dependency. It can fail entirely, time out, or return incomplete or partly incorrect information. A decision is needed about what happens to the entry when the LLM call does not succeed cleanly — so that a failure in an *optional* feature (the AI analysis) never destroys the *core* thing (the user's own writing).

## Decision

The entry is always saved, independently of the outcome of the LLM call.

- The entry's **title and content are saved to the database first**, before the LLM is called. This guarantees the user's own input is never lost — which matters most for the content, which can be a large amount of text.
- The LLM is then called.
  - **If it succeeds**, the returned information is added to the saved entry.
  - **If it returns partial results**, whatever came back is stored as-is; the missing fields simply stay null. Partial output is not rejected.
  - **If it fails or times out**, nothing further is stored; the entry keeps just its title and content, and the UI shows a message explaining the analysis failed, with a **Re-generate** button letting the user retry.

This is why the AI fields (summary, mood, tags, todos) are **nullable** in the database: an entry can validly exist in a *saved-but-not-analysed* state. The behaviour is realised through the synchronous flow described in **ADR-0002**.

## Alternatives rejected

* **Save the entry only after the LLM returns.** Rejected because all of the user's input would be lost if the call failed. The call can take up to ~5 seconds (or longer), so if the user closed the app before it returned, their entry would be lost too. Tying the user's own data to the success of an unreliable external call is unacceptable.

## Consequences

**What this buys:**

- The user's writing is always preserved, regardless of the LLM's response.
- The app stays usable when the LLM is slow or down — the core journaling feature never depends on the external model.

**What it costs:**

- Two separate database writes instead of one (save the entry, then add analysis).
- Every screen must handle the *not-analysed-yet* state — the list and detail views must cope with null AI fields — and the re-generate path must exist to fill them in later.

## Revisit trigger

Reconsider if the AI analysis ever became essential to an entry's purpose — i.e. if an entry without analysis were worthless. For a journal, the user's own writing is the core value and the AI is an enhancement, so this is unlikely to change.
