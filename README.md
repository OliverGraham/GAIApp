# GAIApp

GAIApp is an experimental Kotlin Multiplatform Compose to-do app for Android and iOS.

The main purpose of this project is to demonstrate an agentic AI development workflow. The app itself is intentionally simple; the focus is the workflow used to build it: ticket-driven development, AI-assisted implementation, automated branch creation, formatting, build validation, pull request creation, and auto-merge.

## Purpose

This repo is a practical experiment in using an AI coding agent as the primary implementation loop.

Most of the app was built by running a local workflow script that selected backlog tickets, prompted Aider, allowed the agent to edit the repo directly, validated the build, and opened pull requests.

The goal was not to build a complex to-do app. The goal was to test how far an agentic workflow could go when paired with:

- Kotlin Multiplatform
- Compose Multiplatform
- Android and iOS targets
- Aider
- Gemini API
- Ticket-driven development
- Automated Git branching
- Automated pull request creation
- Build-gated ticket completion

## App

GAIApp is a basic cross-platform to-do list demo with shared Compose UI.

Current features include:

- Sample task data
- A todo list screen
- Add-task UI
- Task completion toggles
- Empty state handling
- Basic UI polish

The package name is:

```text
com.gai.gaiapp
```

## Platform support

GAIApp is built with Kotlin Multiplatform Compose and runs on both Android and iOS.

The project uses shared Kotlin and shared Compose UI where practical, while still supporting platform-specific app entry points.

## Agentic workflow

The backlog lives at:

```text
tickets/backlog.md
```

The main workflow script is:

```text
scripts/agent-ticket-patch.sh
```

The intended usage is:

```bash
scripts/agent-ticket-patch.sh
```

The script picks the first unfinished ticket from the backlog, creates a branch, asks Aider to edit the repo directly, runs formatting and build checks, marks the ticket complete only after a passing build, pushes the branch, and opens a pull request.

Aider is run through:

```bash
python3 -m aider
```

Gemini is used through `GEMINI_API_KEY`.

## What this project demonstrates

This project demonstrates a lightweight agentic software development loop:

- Backlog-first task selection
- AI-generated code changes
- Direct repository editing through Aider
- Conservative automation around Git, formatting, and builds
- Build-gated ticket completion
- Pull request creation through GitHub CLI
- Human-in-the-loop intervention only when needed

Roughly 90% of the app work came from running the ticket script and letting the agent make code changes directly.

Manual intervention was still needed at times. Examples included fixing import issues and handling Gradle sync problems. Android Studio sync is not a Gradle CLI task, so those cases had to be handled manually.

Gemini free-tier limits also affected the workflow. Aider can make multiple model requests during one shell-level run, so quota and rate limits were hit even when the script only called Aider once. With more available tokens or quota, the agent may have been able to resolve some simple issues on its own, but manual fixes were more practical.

## Workflow constraints

The workflow is intentionally conservative:

- Aider edits repo files directly.
- The script does not parse model output into files.
- The script does not retry failed Aider runs by default.
- The script does not retry failed builds by default.
- The script does not attempt Gradle sync.
- Tickets are only marked done after the build passes.
- Formatting is attempted once with Spotless if available.
- The build is run once.

This keeps the automation simple, inspectable, and easier to trust.

## Backlog style

Tickets use this format:

```markdown
## TODO-007: Add task repository contract
```

When a ticket passes, the script changes it to:

```markdown
## DONE-007: Add task repository contract
```

Completed ticket IDs are also appended to:

```text
.agent/done-tickets.txt
```

## Status

This is a demo project, not a production app.

The goal is to explore a practical AI-assisted development loop for a Kotlin Multiplatform Compose app using simple tickets, direct repo edits, build validation, and normal GitHub pull requests.