# ADR-0003: Spring AI as the LLM integration library

**Status:** Accepted

## Context

A decision has to be made about which Java library to use to communicate with the external LLM. The library is needed to send the prompt, get structured output back, and manage the API connection. Three options were considered: Spring AI, LangChain4j, and Embabel.

- **Spring AI** — Spring's official AI library, made by the Spring team. It integrates with Spring Boot and auto-configuration, and provides the `ChatClient` abstraction for communicating with LLMs.
- **LangChain4j** — a Java library inspired by the Python LangChain project. Independent (not Spring-specific), broader in features and integrations, but heavier and not Spring-native.
- **Embabel** — a new Java agent framework by Rod Johnson (the founder of Spring), built *on top of* Spring AI for multi-step, goal-driven agentic workflows.

## Decision

The application uses **Spring AI**. It is Spring-native and integrates directly with the rest of the Spring ecosystem already used in the project.

## Alternatives rejected

* **LangChain4j** — For this application (a single LLM call that returns a simple structured object), LangChain4j would add unnecessary weight and breadth that the project does not use. Spring AI fits the existing Spring stack: the `ChatClient` is auto-configured from `application.yml` — add the starter, set the API key, and it is ready, with no manual wiring.

* **Embabel** — Embabel is an agent framework built on top of Spring AI, concerned with multi-step, goal-driven agentic workflows. It is the **wrong layer** for this application: Journal makes a single structured LLM call, with no agent, no planning, and no multi-step goal, so Embabel would solve a problem the project does not have. Additionally, it is very new — not yet matured, with sparse documentation and a small community. (It is, however, a candidate for the later agents/MCP phase, P3.6.)

## Consequences

**What this buys:**

- Less complexity, more readable, easier to maintain.
- A lighter dependency and a gentler learning curve, fitting the Spring idioms already used in the project.

**What it costs:**

- A less feature-broad library than LangChain4j, with fewer integrations.
- A framework-specific choice: Spring AI is tied to the Spring ecosystem, whereas LangChain4j is framework-independent.

## Revisit trigger

Reconsider this decision if:

- The project moves away from the Spring ecosystem to another framework — in which case LangChain4j, being framework-independent, would be the better choice.
- Requirements grow toward capabilities Spring AI does not cover well on its own — for example RAG pipelines with vector stores, or complex tool-calling chains — where LangChain4j's broader feature set could earn its weight.
- Requirements grow toward multi-step, goal-driven agentic workflows — in which case adding Embabel as a higher-level layer on top of Spring AI would be the natural step.
