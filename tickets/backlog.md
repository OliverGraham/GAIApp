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

## DONE-005: Add empty state

Show an empty state when there are no tasks.

Requirements:
- If task list is empty, show `No tasks yet`.
- Keep add-task UI visible.

Acceptance:
- Empty state appears only when the list is empty.

## DONE-006: Add simple UI polish

Improve visual layout.

Requirements:
- Add padding.
- Add spacing between rows.
- Add simple card-like task rows if supported by existing Material setup.
- Do not introduce new dependencies.

Acceptance:
- App compiles.
- UI looks more like a basic demo app.

## DONE-007: Add task repository contract

Introduce a repository abstraction for task data.

Requirements:
- Add a `TaskRepository` interface in shared/commonMain.
- Repository should expose tasks as observable state or flow.
- Repository should support adding a task.
- Repository should support toggling task completion.
- Repository should support deleting a task.
- Keep the existing UI behavior working.
- Do not add database persistence in this ticket.

Acceptance:
- App compiles.
- UI no longer owns all task mutation logic directly.
- Task operations go through the repository contract.

## DONE-008: Add in-memory task repository implementation

Move runtime task storage behind the repository abstraction.

Requirements:
- Add an `InMemoryTaskRepository` implementation.
- Store tasks in memory using shared code.
- Seed the repository with the existing sample tasks.
- Implement add, toggle, and delete operations.
- Keep behavior equivalent to the current app.

Acceptance:
- App compiles.
- Existing todo list still works.
- New tasks appear immediately.
- Completion toggles still update immediately.

## DONE-009: Add task use cases

Introduce simple use cases between UI and repository.

Requirements:
- Add use cases for observing tasks, adding a task, toggling completion, and deleting a task.
- Use cases should depend on `TaskRepository`.
- Keep use cases small and focused.
- UI should not call repository methods directly after this ticket.

Acceptance:
- App compiles.
- Todo UI behavior is unchanged.
- Task actions flow through use cases.

## DONE-010: Add Spotless formatting

Add deterministic formatting for Kotlin files.

Requirements:
- Add Spotless Gradle configuration for Kotlin/Kotlin Gradle files.
- Configure formatting for `.kt` and `.kts` files.
- Do not introduce unnecessary style rules.
- Ensure formatting can be run from the command line.

Acceptance:
- `./gradlew spotlessApply` runs successfully.
- `./gradlew spotlessCheck` runs successfully.
- App still compiles.

## TODO-011: Add Todo view model

Move screen state and actions into a view model.

Requirements:
- Add a `TodoViewModel` in shared/commonMain if supported by the current project setup.
- View model should expose UI state for tasks and input text.
- View model should expose actions for text changes, adding tasks, toggling tasks, and deleting tasks.
- `TodoListScreen` should render state and call view model actions.
- Avoid platform-specific view model code unless needed.

Acceptance:
- App compiles.
- UI state is not primarily managed inside the composable.
- Todo screen behavior remains unchanged.

## TODO-012: Add dependency injection setup

Introduce dependency injection for app wiring.

Requirements:
- Add Koin or another lightweight DI setup suitable for Kotlin Multiplatform.
- Register repository, use cases, and view model.
- Keep DI setup in shared code where possible.
- Avoid hardcoded construction inside the UI layer.
- Do not introduce unnecessary dependencies.

Acceptance:
- App compiles.
- Todo screen receives its dependencies through DI or a DI-backed entry point.
- App behavior remains unchanged.

## TODO-013: Add Room persistence dependencies

Add the persistence foundation using Room where supported.

Requirements:
- Add Room dependencies needed for Kotlin Multiplatform.
- Add any required Gradle plugin or compiler configuration.
- Configure shared database code in the appropriate source set.
- Do not migrate the UI to persistent storage yet.
- Keep the app compiling on supported targets.

Acceptance:
- App compiles.
- Room dependencies are configured.
- No unused placeholder implementation files are added.
- Existing in-memory app behavior still works.

## TODO-014: Add task database schema

Create the database model for persisted tasks.

Requirements:
- Add a Room `TaskEntity`.
- Add a Room DAO for reading, inserting, updating, and deleting tasks.
- Add a Room database class.
- Add mapping between `TaskEntity` and the domain `Task` model.
- Keep persistence code separate from UI code.

Acceptance:
- App compiles.
- Database schema exists.
- Domain model remains independent from Room annotations.

## TODO-015: Add platform database builders

Wire database creation for supported platforms.

Requirements:
- Add Android database builder code.
- Add desktop database builder code if the current app supports desktop.
- Keep platform-specific database construction out of common UI code.
- Register the database builder through DI or an equivalent shared entry point.

Acceptance:
- App compiles.
- Database can be constructed on supported platforms.
- Platform-specific code stays in platform source sets.

## TODO-016: Add Room-backed task repository

Implement persistent task storage.

Requirements:
- Add a `RoomTaskRepository` implementation.
- Repository should read tasks from the DAO.
- Repository should add tasks to the database.
- Repository should toggle completion in the database.
- Repository should delete tasks from the database.
- Replace the in-memory repository in DI with the Room-backed repository.

Acceptance:
- App compiles.
- Tasks persist after app restart on supported platforms.
- Existing todo UI works without direct database access.

## TODO-017: Add loading and error UI state

Make the UI handle real data-layer states.

Requirements:
- Add loading state to the todo screen state.
- Add error state to the todo screen state.
- Show a simple loading indicator while tasks are loading.
- Show a simple error message if task loading or mutation fails.
- Keep add-task UI usable when appropriate.

Acceptance:
- App compiles.
- UI can represent loading, success, empty, and error states.
- Data-layer failures do not crash the UI.

## TODO-018: Add delete task UI

Allow tasks to be removed.

Requirements:
- Add a delete action for each task row.
- Deleting a task should remove it from the list immediately after success.
- Delete should go through the view model and use case layer.
- Keep the empty state behavior correct after deleting the last task.

Acceptance:
- App compiles.
- Users can delete tasks.
- Empty state appears when all tasks are deleted.

## TODO-019: Add basic repository tests

Add tests for task repository behavior.

Requirements:
- Add tests for adding tasks.
- Add tests for toggling task completion.
- Add tests for deleting tasks.
- Prefer testing shared logic where practical.
- Use an in-memory or test database if Room is involved.

Acceptance:
- Tests run successfully.
- Repository behavior is covered.
- Tests do not depend on manual app interaction.

## TODO-020: Add use case and view model tests

Add tests for the application layer.

Requirements:
- Test add-task validation.
- Test task toggle behavior.
- Test delete behavior.
- Test empty state behavior.
- Test loading or error state behavior where practical.

Acceptance:
- Tests run successfully.
- View model/use case behavior is covered.
- UI logic is less dependent on manual testing.

## TODO-021: Add architecture cleanup pass

Clean up package structure and naming.

Requirements:
- Organize code into clear layers such as domain, data, presentation, and di.
- Keep domain models free from persistence annotations.
- Keep UI code free from direct database access.
- Remove obsolete sample-only wiring if it is no longer used.
- Keep existing behavior intact.

Acceptance:
- App compiles.
- Package structure reflects the app architecture.
- No obvious dead code remains from earlier in-memory-only implementation.