---
name: migrate-to-viewbinding
description: This skill should be used when the user asks to "migrate from Kotlin synthetic to ViewBinding", "migrate Kotlin Android Extensions", "remove kotlinx.android.synthetic", "convert synthetic to view binding", "replace synthetic imports", "migrate to view binding", "analyze synthetic usage", or "get rid of synthetic". Performs systematic migration of Kotlin Synthetic view references to Jetpack ViewBinding in the stepik-android project using the by.kirich1409.viewbindingdelegate library.
version: 1.0.0
---

# Migrate from Kotlin Synthetic to ViewBinding

## Overview

Guide the systematic migration of Kotlin Android Extensions (synthetic) view references to Jetpack ViewBinding in the stepik-android project. The migration follows a three-phase approach: analyze the codebase and create a tracking file, iteratively migrate each file, and optionally upgrade the ViewBindingPropertyDelegate library.

**Constraints:**

- Migrate ONLY files using `kotlinx.android.synthetic.*` imports
- Do NOT migrate files using `findViewById` directly (legacy Java or Kotlin)
- Do NOT touch files that already use ViewBinding
- Use the existing `by.kirich1409.viewbindingdelegate` library (v1.4.7, no-reflection variant)
- Follow the project's established ViewBinding naming conventions

**Project context:**

- Kotlin 1.7.10, AGP 8.6.1, viewBinding enabled in `app/build.gradle` (lines 153-155)
- ViewBindingPropertyDelegate: `com.github.kirich1409:viewbindingpropertydelegate-noreflection:1.4.7`
- Package: `by.kirich1409.viewbindingdelegate`
- Binding classes generated in `org.stepic.droid.databinding` package
- ~201 files with synthetic imports, ~22 files already using viewBinding
- Existing project examples to reference: DebugFragment, RubricatorActivity, CoursePurchaseBottomSheetDialogFragment, CourseNewsAdapterDelegate

## Phase 1: Analysis and Tracking

### Step 1: Scan the project

Run a comprehensive scan for all Kotlin files importing synthetic:

```bash
grep -rn "kotlinx.android.synthetic" --include="*.kt" -l | sort
```

For each discovered file, extract:

- **File type**: Activity, Fragment, DialogFragment, BottomSheetDialogFragment, Adapter/ViewHolder, or utility/helper
- **Layout file**: Derived from the synthetic import (e.g., `kotlinx.android.synthetic.main.activity_auth_social.*` → layout `activity_auth_social`)
- **Binding class**: PascalCase of layout + "Binding" (e.g., `ActivityAuthSocialBinding`)
- **Migration needed**: Confirm the file actually uses synthetic view references (not just type imports)

### Step 2: Create the migration tracker

Create `docs/migrate-to-viewbinding/MIGRATION_TRACKER.md` with the following exact format:

> **Note:** The `docs/` directory is listed in `.gitignore` — this tracker file will NOT be committed to git and will not conflict with the APPS-3860 branch.

```markdown
# Kotlin Synthetic → ViewBinding Migration Tracker

> Generated: YYYY-MM-DD
> Project: stepik-android

## Progress Summary

| Metric | Count |
|--------|-------|
| Total files with synthetic imports | N |
| Needs migration | N |
| Skipped (no direct usage / non-UI) | N |
| Completed | N |
| Errors / Blocked | N |

## Migration Queue

| # | File Path | Type | Layout | Binding Class | Status | Comments |
|---|-----------|------|--------|---------------|--------|----------|
| 1 | `app/src/main/java/...` | Activity | `activity_xxx` | `ActivityXxxBinding` | ⏳ Pending | |
```

**Status values** (use emoji markers for quick scanning):

- `⏳ Pending` — needs migration, not started
- `🔄 In Progress` — actively being migrated
- `✅ Done` — migration complete
- `⏭️ Skip` — does not need migration (verify before marking)
- `⚠️ Blocked` — error or dependency issue

Update the progress summary counts whenever a status changes.

### Step 3: Present the analysis to the user

After creating the tracking file, show the user:
- Total count of files needing migration
- Distribution by type (Activities, Fragments, Dialogs, Adapters, etc.)
- Any files flagged as potential skips (no direct synthetic usage, utility classes)
- Ask the user to confirm before starting the migration cycle

## Phase 2: Migration Cycle

Process each file marked `⏳ Pending` sequentially. For each file:

### 2.1: Mark as In Progress

Update the tracking file status to `🔄 In Progress`.

### 2.2: Read and analyze the file

Read the target file completely. Identify:

- All synthetic import lines (including wildcard `.*` and specific view imports like `kotlinx.android.synthetic.main.layout.viewName`)
- Every synthetic view reference in the code body
- The class hierarchy (determines which pattern to use — Activity, Fragment, DialogFragment, BottomSheetDialogFragment, Adapter, utility)
- Any complex patterns (view references in lambdas, companion objects, extension functions, or delegated properties)
- Whether the file imports from multiple layouts (requires multiple binding classes or includes)

### 2.3: Determine binding class and layout

Extract the binding class name from the synthetic import:

```
import kotlinx.android.synthetic.main.activity_auth_social.*
→ Layout: activity_auth_social
→ Binding class: ActivityAuthSocialBinding
→ Import: org.stepic.droid.databinding.ActivityAuthSocialBinding
```

Verify the layout file exists at `app/src/main/res/layout/<layout_name>.xml` (or in the appropriate source set). If the layout file does not exist, the synthetic import was invalid — mark the file as `⏭️ Skip`.

### 2.3a: Remove `tools:viewBindingIgnore` from layout XML

**CRITICAL STEP:** Many layout XML files in this project have `tools:viewBindingIgnore="true"` on the root element. This attribute was added when the project used synthetic views and tells the Android build system to **skip generating** ViewBinding classes for that layout. Without removing it, the binding class **will not be generated** and the migration will fail with "Unresolved reference" errors.

For each layout file referenced by the Kotlin file being migrated:

1. Read the layout XML file
2. Check if the root element has `tools:viewBindingIgnore="true"`
3. If present, remove it (but keep the `xmlns:tools` namespace if other `tools:` attributes exist in the file)
4. If `xmlns:tools="http://schemas.android.com/tools"` is only used by `viewBindingIgnore` and no other `tools:` attributes remain, remove the namespace declaration too

**Before:**
```xml
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    tools:viewBindingIgnore="true">
```

**After (if other tools: attributes exist):**
```xml
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
```

**After (if no other tools: attributes):**
```xml
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
```

> **Important:** If the Kotlin file imports from multiple layouts (e.g., `import kotlinx.android.synthetic.main.fragment_lesson.*` and `import kotlinx.android.synthetic.main.layout_video_controls.*`), check and fix **each** layout file.

### 2.3b: Check `<include>` layouts for `viewBindingIgnore` and handle `.root` access

**CRITICAL STEP:** When a layout uses `<include layout="@layout/xxx">`, the included layout may also have `tools:viewBindingIgnore="true"`. If the code accesses views from the included layout (either directly or via a delegate/helper class that takes the include's root `View`), you must:

1. **Check all `<include>`d layouts** in the parent layout XML for `tools:viewBindingIgnore="true"`
2. **Remove it** from each included layout that is referenced by the migrated code
3. **Use `.root` when passing an included layout to a delegate** that expects a `View` parameter

**How `<include>` works with ViewBinding:**

When a parent layout includes another layout:
```xml
<!-- view_achievement_item.xml -->
<RelativeLayout ...>
    <include
        layout="@layout/view_achievement_tile"
        android:id="@+id/achievementTile"
        android:layout_width="64dp"
        android:layout_height="wrap_content" />

    <TextView
        android:id="@+id/achievementTitle" ... />
</RelativeLayout>
```

The parent binding exposes the included layout via its own binding class:
- `binding.achievementTile` → type is `ViewAchievementTileBinding` (the included layout's binding)
- `binding.achievementTile.root` → type is `View` (the root view of the included layout)
- `binding.achievementTitle` → direct child views are accessible normally

**When to use `.root`:**
- When passing the included layout to a delegate/helper that takes a `View` parameter:
  ```kotlin
  // Delegate expects a View (the include's root)
  private val tileDelegate = AchievementTileDelegate(binding.achievementTile.root, resolver)
  ```
- When a helper class uses synthetic imports on the included layout (e.g., `root.achievementLevels`), pass `.root` not the binding itself

**When NOT to use `.root`:**
- When accessing specific views within the included layout directly through its binding:
  ```kotlin
  binding.achievementTile.achievementLevels.progress = 5
  ```

**Before migration (synthetic):**
```kotlin
import kotlinx.android.synthetic.main.view_achievement_item.view.*
import kotlinx.android.synthetic.main.view_achievement_tile.view.*

// root.achievementTile is the include's root View (synthetic generates it as a View)
// root.achievementTitle is a direct child
val tileDelegate = AchievementTileDelegate(root.achievementTile, resolver)
```

**After migration (viewBinding):**
```kotlin
// binding.achievementTile is ViewAchievementTileBinding (not View)
// binding.achievementTile.root is the include's root View (equivalent to synthetic root.achievementTile)
// binding.achievementTitle is a direct child (same as before)
val tileDelegate = AchievementTileDelegate(binding.achievementTile.root, resolver)
```

> **Important:** Scan the entire layout XML for all `<include>` tags. For each included layout, check if `tools:viewBindingIgnore="true"` exists and remove it if the migrated code (or any delegate/helper it uses) references views from that included layout.

### 2.4: Apply the migration pattern

Follow the pattern matching the class type. See **`references/patterns.md`** for detailed patterns with before/after examples for each type: Activity, Fragment, DialogFragment, BottomSheetDialogFragment, Adapter/ViewHolder, and utility classes.

**General migration steps:**

1. Remove `tools:viewBindingIgnore="true"` from the layout XML root element (see step 2.3a)
2. Remove all `kotlinx.android.synthetic.*` import lines
3. Add `import by.kirich1409.viewbindingdelegate.viewBinding`
4. Add import for the generated binding class (`org.stepic.droid.databinding.<BindingClassName>`)
5. Declare the binding property: `private val <feature>Binding: <BindingClass> by viewBinding(<BindingClass>::bind)`
6. Replace all synthetic view references: `viewId` → `binding.viewId`
7. Convert snake_case view IDs to camelCase: `root_view` → `rootView`, `sign_in_button` → `signInButton`
8. Preserve existing layout inflation (`setContentView` for Activities, `onCreateView` return for Fragments)

**Naming conventions in the project:**

- Binding property names use a descriptive prefix: `debugBinding`, `courseNewsBinding`, `rubricatorBinding`, `filterRootBinding`
- For ViewHolders, use `viewBinding` as the property name
- The prefix typically matches the feature or screen name, not just the class name

### 2.5: Handle edge cases

Before finalizing, check for these common edge cases:

- **Views accessed via `this.viewId`** — some files use `this` qualifier; replace with `binding.viewId`
- **Views accessed via `view?.viewId`** — nullable access in Fragments; use `binding.viewId` (non-null after onViewCreated)
- **Views accessed in `onCreate` before `setContentView`** — move access after layout inflation, or use `findViewById` temporarily
- **Views passed to other classes/functions** — pass `binding.viewId` instead of the bare view reference
- **Extension functions on views** — these still work; just call on `binding.viewId` instead
- **Multiple synthetic imports from different layouts** — check if layouts are `<include>`d; if so, one binding may cover all views. Otherwise, create separate binding properties.

### 2.6: Verify compilation

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -50
```

If compilation fails, analyze the error and fix before proceeding. See **`references/testing-guide.md`** for common errors and their fixes.

### 2.7: Update tracking file

Set status to `✅ Done`. Add any notable comments:

- Non-standard patterns encountered
- Files that required special handling
- Related files that may need attention
- Any warnings or concerns

Example tracking file entry after completion:

```markdown
| 1 | `app/src/.../SocialAuthActivity.kt` | Activity | `activity_auth_social` | `ActivityAuthSocialBinding` | ✅ Done | 14 view references migrated. Uses SmartLockActivityBase. |
```

### 2.8: Proceed to next file

Continue until all `⏳ Pending` files are processed.

### 2.9: Commit strategy

Recommend committing after each file or batch of 5-10 migrated files. Use the commit message format:

```
refactor: migrate <FileName> from synthetic to viewBinding
```

For batch commits:

```
refactor: migrate N files from synthetic to viewBinding

- File1.kt
- File2.kt
- ...
```

## Phase 3: Library Upgrade Offer

After all migrations are complete, offer the user the option to upgrade the ViewBindingPropertyDelegate library. See **`references/library-migration.md`** for full details.

**Key constraint:** The new library v2.0.4 requires Kotlin 2.1.10+. The current project uses Kotlin 1.7.10. The library upgrade is NOT compatible without first upgrading Kotlin. Inform the user of this incompatibility and the required steps.

**Summary of v2.x changes:**

- New Maven coordinates: `dev.androidbroadcast.vbpd:vbpd:2.0.4`
- New package: `dev.androidbroadcast.vbpd`
- Import change: `import dev.androidbroadcast.vbpd.viewBinding`
- The delegate usage pattern (`viewBinding(XxxBinding::bind)`) remains identical
- Breaking changes: `onViewDestroyed` callback removed, `strictMode` removed
- Minimum Kotlin 2.1.10, minimum AGP 8.8.0

## Working Examples in the Project

Reference these existing viewBinding implementations in the project as gold-standard examples:

**Fragment:** `app/src/main/java/org/stepik/android/view/course_news/ui/fragment/CourseNewsFragment.kt`
- Uses `private val courseNewsBinding: FragmentCourseNewsBinding by viewBinding(FragmentCourseNewsBinding::bind)`
- Demonstrates binding usage in `onViewCreated`, `ViewStateDelegate`, and `render` method

**Activity:** `app/src/main/java/org/stepik/android/view/rubricator/ui/RubricatorActivity.kt`
- Uses `private val rubricatorBinding: ActivityRubricatorBinding by viewBinding(ActivityRubricatorBinding::bind)`
- Demonstrates nested binding access: `rubricatorBinding.rubricatorAppbar.viewCenteredToolbarBinding.centeredToolbar`
- Keeps `setContentView(R.layout.activity_rubricator)` in `onCreate`

**BottomSheetDialogFragment:** `app/src/main/java/org/stepik/android/view/course_purchase/ui/dialog/CoursePurchaseBottomSheetDialogFragment.kt`
- Uses `private val coursePurchaseBinding: BottomSheetDialogCoursePurchaseBinding by viewBinding(BottomSheetDialogCoursePurchaseBinding::bind)`
- Demonstrates passing binding to delegate classes

**Adapter ViewHolder:** `app/src/main/java/org/stepik/android/view/course_news/ui/adapter/delegate/CourseNewsAdapterDelegate.kt`
- Uses `private val viewBinding: ItemCourseNewsBinding by viewBinding { ItemCourseNewsBinding.bind(root) }`
- Demonstrates binding inside adapter delegate ViewHolder

## Quick Reference

### Binding declaration by type

**The `viewBinding()` delegate uses the SAME import and SAME syntax for all lifecycle-aware component types** (Activity, Fragment, DialogFragment, BottomSheetDialogFragment). The library defines separate extension functions per receiver type, resolved by Kotlin at the call site. The only exception is ViewHolder which uses the lambda form.

```
Fragment/Activity/DialogFragment/BottomSheetDialog (all the same):
  private val binding: XxxBinding by viewBinding(XxxBinding::bind)

Adapter ViewHolder (lambda form — no lifecycle to auto-bind):
  private val viewBinding: ItemXxxBinding by viewBinding { ItemXxxBinding.bind(root) }
```

### Import changes (same for ALL component types)

```
REMOVE:  import kotlinx.android.synthetic.main.layout_name.*
ADD:     import by.kirich1409.viewbindingdelegate.viewBinding        ← one import for everything
ADD:     import org.stepic.droid.databinding.<BindingClassName>
```

> **Note:** A deprecated `dialogViewBinding()` exists in v1.4.7 (separate import `dialogViewBinding`) that delegates to `viewBinding()`. Never use it — always use `viewBinding()` for all types.

### View ID naming conversion

```
XML ID:      android:id="@+id/sign_in_button"
Synthetic:   signInButton  (accessible as top-level property)
Binding:     binding.signInButton  (accessible via binding instance)
```

### Determining if a file needs migration

| Scenario | Action |
|----------|--------|
| Has `import kotlinx.android.synthetic.*` | ✅ Needs migration |
| Uses `findViewById` only (no synthetic import) | ⏭️ Skip — not in scope |
| Already has `by.kirich1409.viewbindingdelegate` import | ⏭️ Skip — already migrated |
| Java file (`.java`) with `findViewById` | ⏭️ Skip — not in scope |
| Has synthetic import but no view references | ⏭️ Skip — remove unused import only |

## Additional Resources

### Reference Files

- **`references/patterns.md`** — Detailed migration patterns with before/after examples for Activity, Fragment, DialogFragment, BottomSheetDialogFragment, Adapter/ViewHolder, and utility classes
- **`references/library-migration.md`** — ViewBindingPropertyDelegate v1.x → v2.x upgrade guide with compatibility matrix and step-by-step migration instructions
- **`references/testing-guide.md`** — Compilation verification, common errors and fixes, test commands, and manual verification checklist
