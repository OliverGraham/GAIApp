# Backlog

## DONE-001: Add basic task model

Create a shared task model for the app.

Requirements:
- Add a `Task` data class in shared/commonMain.
- Fields: `id`, `title`, `isDone`.
- Add a small helper for creating sample tasks.
- Do not add persistence yet.

Acceptance:
- App compiles.
- Sample tasks can be used by UI.

## DONE-002: Add todo list screen

Create a Compose screen that displays todo items.

Requirements:
- Add a `TodoListScreen` composable.
- Show a title: `Todo List`.
- Show sample tasks in a vertical list.
- Each row shows the task title.
- Completed tasks should show a checked indicator.

Acceptance:
- Screen uses shared Compose code.
- No empty placeholder classes.

## DONE-003: Add add-task UI state

Add local UI state for creating tasks.

Requirements:
- Add a text field for entering a task title.
- Add an `Add` button.
- When tapped, add a new item to the in-memory list.
- Empty titles should not be added.

Acceptance:
- User can add tasks during runtime.
- No persistence required.

## DONE-004: Add task completion toggle

Allow tasks to be marked complete.

Requirements:
- Each task row should be clickable or have a checkbox.
- Tapping toggles `isDone`.
- Completed state should update on screen.

Acceptance:
- State updates immediately.
- Existing task list remains visible.

## TODO-005: Add empty state

Show an empty state when there are no tasks.

Requirements:
- If task list is empty, show `No tasks yet`.
- Keep add-task UI visible.

Acceptance:
- Empty state appears only when the list is empty.

## TODO-006: Add simple UI polish

Improve visual layout.

Requirements:
- Add padding.
- Add spacing between rows.
- Add simple card-like task rows if supported by existing Material setup.
- Do not introduce new dependencies.

Acceptance:
- App compiles.
- UI looks more like a basic demo app.