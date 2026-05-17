# GAIApp

GAIApp is an experimental Kotlin Multiplatform Compose to-do app.

The app is intentionally simple. It is a demo project for testing a local-ish, ticket-driven agentic development workflow using Aider and Gemini.

## App

GAIApp is a basic to-do list app with shared Compose UI.

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

## Agentic workflow

Most of this project was built by running an agent script against backlog tickets.

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

## What this project is testing

This repo is testing whether a small app can be built mostly by letting an agent work through a structured backlog.

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

This keeps the automation simple and easier to inspect.

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

The goal is to explore a practical agent-assisted development loop for a Kotlin Multiplatform Compose app using simple tickets, direct repo edits, and normal GitHub pull requests.