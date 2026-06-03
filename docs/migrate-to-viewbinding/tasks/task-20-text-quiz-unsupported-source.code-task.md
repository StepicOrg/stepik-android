---
status: pending
created: 2026-06-03
started: null
completed: null
---
# Task 20: Migrate Text Quiz Unsupported Source from Synthetic to ViewBinding

## Description
Migrate 5 Kotlin source files from `kotlinx.android.synthetic` to ViewBinding using the `by.kirich1409.viewbindingdelegate` library.

## Files to Migrate

1. `app/src/main/java/org/stepik/android/view/step_quiz_table/ui/fragment/TableStepQuizFragment.kt`
2. `app/src/main/java/org/stepik/android/view/step_quiz_text/ui/delegate/TextStepQuizFormDelegate.kt`
3. `app/src/main/java/org/stepik/android/view/step_quiz_text/ui/fragment/TextStepQuizFragment.kt`
4. `app/src/main/java/org/stepik/android/view/step_quiz_unsupported/ui/fragment/UnsupportedStepQuizFragment.kt`
5. `app/src/main/java/org/stepik/android/view/step_source/ui/dialog/EditStepSourceDialogFragment.kt`

## Background
This is part of the ongoing Kotlin Synthetic → ViewBinding migration for the stepik-android project. Kotlin Android Extensions (including synthetic view binding) is deprecated and must be replaced with Jetpack ViewBinding.

## Migration Instructions

### Step 1: Load the migration skill
Before starting, load the `migrate-to-viewbinding` skill to ensure proper migration patterns:
```
/migrate-to-viewbinding
```

### Step 2: Migrate each file
For each file listed above:
1. Read the current file
2. Apply the ViewBinding migration pattern (as specified by the skill)
3. Remove synthetic imports, add ViewBinding property, replace view references

### Step 3: Verify build
Run the build verification command:
```bash
./gradlew :app:assembleDebug --rerun-tasks -Pkotlin.compiler.execution.strategy=in-process
```

### Step 4: Update tracker
Update the migration status in `docs/migrate-to-viewbinding/MIGRATION_TRACKER.md` for each file:
- Set status to `✅ Done` (or `⏳ In Progress` during migration)
- Add comments about what was changed

### Step 5: Commit
Commit all changes with message: `refactor: migrate 5 files from synthetic to viewBinding`

## Constraints
- **DO NOT** take additional files from the tracker — only migrate the 5 files listed above
- **DO NOT** skip loading the migration skill
- **DO NOT** skip the build verification step
- **ALWAYS** update the tracker file after migration
- **ALWAYS** commit after completing all 5 files

## Dependencies
- The `migrate-to-viewbinding` skill must be loaded first
- Build must pass before committing

## Acceptance Criteria

1. **All 5 files migrated**
   - Given 5 source files using synthetic imports
   - When the migration skill is applied to each file
   - Then all synthetic imports are removed and replaced with ViewBinding

2. **Build passes**
   - Given all 5 files have been migrated
   - When `./gradlew :app:assembleDebug --rerun-tasks -Pkotlin.compiler.execution.strategy=in-process` is run
   - Then the build completes successfully with no errors

3. **Tracker updated**
   - Given migration is complete
   - When checking `MIGRATION_TRACKER.md`
   - Then all 5 files show `✅ Done` status with comments

4. **Changes committed**
   - Given build passes and tracker is updated
   - When the commit is made
   - Then the commit message is `refactor: migrate 5 files from synthetic to viewBinding`

## Ready-to-use Prompt

```
Migrate these 5 files from Kotlin synthetic to ViewBinding. Load the /migrate-to-viewbinding skill first.

Files to migrate:
- app/src/main/java/org/stepik/android/view/step_quiz_table/ui/fragment/TableStepQuizFragment.kt
- app/src/main/java/org/stepik/android/view/step_quiz_text/ui/delegate/TextStepQuizFormDelegate.kt
- app/src/main/java/org/stepik/android/view/step_quiz_text/ui/fragment/TextStepQuizFragment.kt
- app/src/main/java/org/stepik/android/view/step_quiz_unsupported/ui/fragment/UnsupportedStepQuizFragment.kt
- app/src/main/java/org/stepik/android/view/step_source/ui/dialog/EditStepSourceDialogFragment.kt

After migrating, run: ./gradlew :app:assembleDebug --rerun-tasks -Pkotlin.compiler.execution.strategy=in-process
Update docs/migrate-to-viewbinding/MIGRATION_TRACKER.md for each file.
Commit with: refactor: migrate 5 files from synthetic to viewBinding
```

## Metadata
- **Complexity**: Medium
- **Labels**: migration, viewbinding, refactoring
- **Required Skills**: migrate-to-viewbinding skill, Android ViewBinding, Kotlin
