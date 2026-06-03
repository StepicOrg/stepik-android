# PROMPT.md: Kotlin Synthetic → ViewBinding Migration (Autonomous)

## Objective

Autonomously migrate all remaining 108 Kotlin files from `kotlinx.android.synthetic` to Jetpack ViewBinding using the `by.kirich1409.viewbindingdelegate` library, processing 5 files per loop iteration.

## Task Registry

22 task files are defined in `docs/migrate-to-viewbinding/tasks/`:
- `task-01-catalog-certificate-ui.code-task.md` (5 files)
- `task-02-comments-submissions.code-task.md` (5 files)
- `task-03-course-core.code-task.md` (5 files)
- `task-04-course-info-collection-lists.code-task.md` (5 files)
- `task-05-course-list-fragments-download.code-task.md` (5 files)
- `task-06-course-revenue.code-task.md` (5 files)
- `task-07-transaction-reviews.code-task.md` (5 files)
- `task-08-reviews-learning-actions.code-task.md` (5 files)
- `task-09-lesson-onboarding-stories.code-task.md` (5 files)
- `task-10-profile-core.code-task.md` (5 files)
- `task-11-profile-details-edit.code-task.md` (5 files)
- `task-12-profile-edit-solutions.code-task.md` (5 files)
- `task-13-solutions-step-delegates.code-task.md` (5 files)
- `task-14-step-core-quiz-base.code-task.md` (5 files)
- `task-15-step-quiz-choice.code-task.md` (5 files)
- `task-16-fill-blanks-quiz.code-task.md` (5 files)
- `task-17-code-fullscreen-matching.code-task.md` (5 files)
- `task-18-sorting-sql-quiz.code-task.md` (5 files)
- `task-19-table-quiz.code-task.md` (5 files)
- `task-20-text-quiz-unsupported-source.code-task.md` (5 files)
- `task-21-submissions-user-reviews.code-task.md` (5 files)
- `task-22-user-reviews-final-video-player.code-task.md` (3 files — final batch)

**Execution order:** task-01 → task-02 → ... → task-22 (sequential)

## Loop Workflow (per iteration)

Each loop iteration processes exactly **one task file** (5 source files). Follow this workflow:

### 1. Load Migration Skill
```
/migrate-to-viewbinding
```
**CRITICAL:** Always load this skill at the start of every loop iteration. Without it, the migration will have incorrect patterns.

### 2. Determine Next Task
- Read `docs/migrate-to-viewbinding/MIGRATION_TRACKER.md` to identify which files are still pending
- Pick the next task file from the registry above that has not been completed
- Read the task file to get the exact 5 files to migrate

### 3. Migrate Files
For each of the 5 files in the current task:
- Read the source file
- Apply the ViewBinding migration pattern as taught by the skill
- Remove `kotlinx.android.synthetic` imports
- Add ViewBinding property (using `by viewBinding()` for Fragments, `by viewBinding()` for Activities, etc.)
- Replace all synthetic view references with `binding.xxx`
- Remove `viewBindingIgnore` attributes from XML layouts if present
- Remove `LayoutContainer` interface and `containerView` if present in adapter delegates

### 4. Verify Build
```bash
./gradlew :app:assembleDebug --rerun-tasks -Pkotlin.compiler.execution.strategy=in-process
```
**If the build fails:** Fix the errors and re-run. Do NOT commit until the build passes.

### 5. Update Tracker
Update `docs/migrate-to-viewbinding/MIGRATION_TRACKER.md`:
- Change status from `⏳ Pending` to `✅ Done` for each migrated file
- Add relevant comments (e.g., "Removed LayoutContainer", "Removed viewBindingIgnore", number of view refs)

### 6. Commit
```bash
git add -A && git commit -m "refactor: migrate 5 files from synthetic to viewBinding"
```

### 7. Hand Off to Next Loop
- Update `.ralph/agent/handoff.md` with completed task
- Exit the loop — the next iteration will pick up the next task

## Constraints

- **STRICT SCOPE:** Only migrate the exact 5 files listed in the current task. Do NOT pick additional files.
- **NO SKIP:** Never skip loading the `/migrate-to-viewbinding` skill.
- **BUILD GATE:** Never commit if the build fails.
- **TRACKER ACCURACY:** Always update the tracker — it is the source of truth for progress.
- **EXCLUDED FILES:** Never touch the 4 APPS-3860 branch files listed in the tracker.
- **ONE TASK PER LOOP:** Each loop handles exactly one task (5 files). Then start a fresh loop.

## Acceptance Criteria

The migration is complete when:
1. All 22 tasks have been processed (108 files migrated)
2. `grep -rl "kotlinx.android.synthetic" --include="*.kt" app/src/` returns only the 4 excluded APPS-3860 files (or is empty)
3. All tasks in `MIGRATION_TRACKER.md` show `✅ Done` (except the 4 excluded files)
4. All commits have been made with consistent message format
5. The final build passes cleanly

## Ralph Command

```bash
ralph run -c ralph.yml -H builtin:code-assist
```

Or for the full pipeline:
```bash
ralph run --config presets/pdd-to-code-assist.yml
```
