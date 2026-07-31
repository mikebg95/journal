# ADR-0002: Synchronous AI processing

**Status:** Accepted

## Context

A decision has to be made about synchronous vs asynchronous communication in the Journal application. The external LLM dependency is slow and can fail — it can take up to 5 seconds (or possibly more) to return a response.

The choice is whether to let the user wait, freezing the application while the LLM runs (simpler, but inferior UX), or to save the user's input immediately and let the app continue while the call runs in the background, updating the UI once the response returns (better UX, but more complex).

## Decision

The application uses **synchronous** communication.

The flow is:

1. The user enters the entry title and content and clicks save.
2. The screen freezes, showing a loading spinner or a short waiting message.
3. The backend saves the entry with just the title and content.
4. The backend calls the external LLM.
5. If the LLM succeeds, its generated information (summary, tags, mood, todos) is added to the saved entry.
6. If the LLM fails, nothing further is saved; the UI shows a message such as *"Generating additional information failed. Please try again,"* with a **Re-generate** button (see ADR-0005).
7. The UI updates and the screen unfreezes.

Saving the title and content first (step 3) means the user's own input is never lost, even if the LLM call fails.

## Alternatives rejected

* **Asynchronous communication.** Asynchronous communication would actually give *better* UX: the entry saves instantly and the AI-generated fields appear a few seconds later, with no frozen screen. It is rejected not for UX, but for the implementation complexity it adds.

  With asynchronous communication the user enters the title and content, which is saved immediately; the UI updates and the user can continue, while the application calls the LLM in the background. This creates several complexity costs:

  - The AI fields are not visible for a few seconds, so the app must build a live "analysing…" status to show that work is happening in the background — otherwise it looks like something went wrong.
  - The user could edit the entry while the LLM is still running, triggering a second call. Handling this needs extra machinery — e.g. locking the entry from edits until the response returns, or cancelling and restarting the analysis.
  - The app needs a way to notify the UI when the AI finishes — polling or server-sent events (SSE).
  - The app needs to handle analysis that fails *after* the user has already moved on: where and how is that error surfaced?

  Each of these is a real build cost. For a single-user portfolio app, that complexity is not worth trading for the (better) asynchronous UX.

## Consequences

**What this buys:**

- Less complexity. No mechanism needed to stop the user editing an entry while the LLM is still generating a response, and no live "in progress" UI construction.

**What it costs:**

- The user is blocked (frozen screen) until the LLM resolves — up to ~5s, or longer if the model is slow. The entry's title and content are saved first so they are never lost, but the user still waits for the analysis before continuing. This is mitigated by an explicit timeout and graceful degradation (see ADR-0005).

## Revisit trigger

Reconsider this decision if the LLM dependency turns out to be substantially slower (e.g. 10–15 seconds), which would harm the UX enough that users might quit thinking the app is broken — or if the application gains more than one real user, since concurrent load makes asynchronous processing more valuable.
