# Kotlin 1.9.25 Migration Tracker

Last updated: 2026-06-14.

## Goal

Upgrade the project from Kotlin `1.7.10` to Kotlin `1.9.25` without losing the
recent migration work from Kotlin Android Extensions synthetics to ViewBinding
and from old Kotlin parcel APIs to `kotlin-parcelize`.

This tracker records confirmed local findings, library risks, and the expected
order of work.

## Current Build Baseline

Current local project state:

- Gradle wrapper: `8.7` in `gradle/wrapper/gradle-wrapper.properties`.
- Android Gradle Plugin: `8.6.1` in `dependencies.gradle`.
- Kotlin Gradle plugin: `1.7.10` in `dependencies.gradle`.
- `compileSdk` / `targetSdk`: `35`.
- `:app` applies `kotlin-android`, `kotlin-kapt`, and `kotlin-parcelize`.
- `:model` applies `kotlin-android`, `kotlin-kapt`, and `kotlin-parcelize`.
- `:billing` applies `kotlin-android` and `kotlin-kapt`.
- `:androidsvg` is Java-only and does not apply Kotlin plugins.

Use this build command during the migration:

```bash
./gradlew :app:assembleDebug --rerun-tasks -Pkotlin.compiler.execution.strategy=in-process
```

This command is currently used to avoid Kotlin compilation errors caused by the
temporary mismatch between Kotlin, Android Gradle Plugin, and Gradle versions.
After the migration is complete and the versions are aligned, the usual build
and compilation commands should work again without this workaround.

## Confirmed Source Migration State

Already done:

- No remaining `kotlinx.android.synthetic` imports were found.
- No remaining old `kotlinx.android.parcel` or `kotlin.android.parcel` imports
  were found.
- `kotlin-parcelize` is already applied in `:app` and `:model`.
- Existing `@Parcelize` imports use `kotlinx.parcelize`.

Done:

- Removed remaining `kotlinx.android.extensions.LayoutContainer` usage:
  - `app/src/main/java/org/stepik/android/view/course_list/ui/adapter/delegate/CourseListViewAllAdapterDelegate.kt`
  - `app/src/main/java/org/stepik/android/view/user_reviews/ui/adapter/delegate/UserReviewsPlaceholderAdapterDelegate.kt`

Both classes now use plain `DelegateViewHolder(...)` implementations.
They no longer use synthetic view access, so `LayoutContainer` is unnecessary.

## Toolchain Compatibility Notes

Kotlin `1.9.25` with AGP `8.6.1` and Gradle `8.7` is a risk area. The local
project has already moved past the old AGP 7.x blocker, but Kotlin/AGP/Gradle
support should still be checked against the official Kotlin Gradle compatibility
matrix before the final version bump.

Reference:

- https://kotlinlang.org/docs/gradle-configure-project.html

Decision to make:

- Either proceed with Kotlin `1.9.25` and keep the temporary in-process compiler
  workaround during migration.
- Or consider a Kotlin version that is officially aligned with the current AGP
  version if the build remains unstable.

## Dependency Findings

Gradle dependency insight showed that runtime Kotlin artifacts currently resolve
to Kotlin stdlib `1.8.22`, even though the project declares Kotlin `1.7.10`.
This happens through transitive dependencies, especially Firebase.

Migration requirement:

- Explicitly align Kotlin artifacts when moving to `1.9.25`.
- Do not let transitive dependencies decide the Kotlin stdlib version.
- Prefer a Kotlin BOM or a resolution strategy that keeps `kotlin-stdlib`,
  `kotlin-stdlib-jdk7`, and `kotlin-stdlib-jdk8` consistent with the compiler.

## High Priority Library Work

### Room

Current version: `androidx.room:room-runtime` / `room-compiler` `2.3.0`.

Risk:

- Old Room compiler runs through `kapt` and is a likely failure point after the
  Kotlin compiler upgrade.

Recommended migration:

- Upgrade Room to at least `2.6.1`; prefer the current stable line after testing.
- Consider moving Room annotation processing from `kapt` to `ksp`.
- Re-run database schema/migration tests after the upgrade.

References:

- https://developer.android.com/jetpack/androidx/releases/room
- https://developer.android.com/build/migrate-to-ksp

### Dagger

Current version: `2.42`.

Risk:

- Dagger compiler runs through `kapt` in `:app` and `:billing`.
- Kotlin compiler upgrades often expose old kapt/annotation processor issues.

Recommended migration:

- Upgrade Dagger before or together with the Kotlin bump.
- Keep `kapt` initially unless there is a dedicated Dagger KSP migration task.
- If moving to KSP later, do it after a successful Kotlin/compiler baseline.

### Glide Compiler

Current version: `com.github.bumptech.glide:compiler` `4.11.0`.

Risk:

- Old annotation processor.

Recommended migration:

- Upgrade Glide and its compiler together.
- If staying on Glide 4.x, use `4.16.0` as the conservative target before
  considering Glide 5.x.

### ktlint and Custom Rules

Current versions:

- `com.pinterest:ktlint:0.34.2`
- `ru.nobird.android.ktlint:rules:1.0.0`

Risk:

- Very old ktlint and custom rule APIs may not work cleanly with modern Kotlin
  syntax/parser behavior.
- Custom rules are private package dependencies and must be validated with
  access to the GitHub Packages repository.

Recommended migration:

- Treat ktlint separately from compilation.
- First get `assembleDebug` and unit tests passing.
- Then update ktlint and custom rules, or temporarily decouple ktlint from the
  Kotlin compiler migration if rule updates are not ready.

## Firebase KTX Migration Risk

Current project uses Firebase KTX dependencies:

- `firebase-analytics-ktx`
- `firebase-config-ktx`

Current code imports KTX APIs such as:

- `com.google.firebase.ktx.Firebase`
- `com.google.firebase.analytics.ktx.analytics`
- `com.google.firebase.remoteconfig.ktx.remoteConfig`
- `com.google.firebase.remoteconfig.ktx.remoteConfigSettings`
- `com.google.firebase.remoteconfig.ktx.get`

Risk:

- Firebase stopped releasing KTX modules and removed them from the Firebase BoM
  starting with BoM `34.0.0` in July 2025.

Recommended migration:

- If Firebase BoM stays on the current `32.3.1`, this is not the first Kotlin
  compiler blocker.
- If Firebase BoM is upgraded as part of the Kotlin/toolchain work, migrate away
  from Firebase KTX APIs to the main Firebase modules first.

Reference:

- https://firebase.google.com/docs/android/kotlin-migration

## Kotlin-Authored Library Compatibility Checks

These libraries currently request older Kotlin stdlib versions transitively.
Older Kotlin metadata is usually readable by Kotlin `1.9.25`, so this is not
automatically fatal. Still, these should be smoke-tested because they are
Kotlin-authored and used heavily in presentation/UI code.

Check or upgrade:

- `com.github.kirich1409:viewbindingpropertydelegate-noreflection:1.4.7`
  - Maven Central metadata lists `1.5.9` as latest.
- `ru.nobird.app.core:model:1.0.8`
  - Dependency insight showed Kotlin stdlib `1.5.31` requested transitively.
- `ru.nobird.app.presentation:presentation-redux:1.3.1`
  - Dependency insight showed Kotlin stdlib `1.5.31` requested transitively.
- `ru.nobird.android.*` AndroidKit packages
  - Private GitHub Packages dependencies; validate available versions with
    credentials.
- `ru.nobird.android:storieskit:1.1.2`
  - Private GitHub Packages dependency; validate available versions with
    credentials.

Lower priority old Kotlin transitive requests:

- `io.reactivex.rxjava2:rxkotlin:2.3.0` requests Kotlin stdlib `1.2.60`.
- `jp.wasabeef:recyclerview-animators:4.0.1` requests Kotlin stdlib `1.3.72`.
- `com.vk:androidsdk:2.2.3` requests Kotlin stdlib `1.3.71`.

These are runtime dependencies rather than compiler plugins. They should be
checked, but they are less likely to block the Kotlin compiler upgrade directly.

## Build Script Cleanup

Remove unnecessary kapt usage:

- `:model` applies `kotlin-kapt`, but `:model:kaptDebug` has no dependencies.
- Remove `apply plugin: 'kotlin-kapt'` from `model/build.gradle` unless a new
  processor is added there.

Review deprecated AGP options after the Kotlin upgrade:

- `android.defaults.buildfeatures.buildconfig=true` is deprecated and should be
  moved into module-level `buildFeatures`.
- `lintOptions`, `packagingOptions`, and other old DSL blocks can be migrated
  later if they are not blocking the Kotlin upgrade.

## Suggested Migration Order

1. LayoutContainer cleanup is complete.
2. Remove unused `kotlin-kapt` from `:model`.
3. Align Kotlin Gradle plugin and Kotlin stdlib artifacts to `1.9.25`.
4. Run the temporary safe build command:

   ```bash
   ./gradlew :app:assembleDebug --rerun-tasks -Pkotlin.compiler.execution.strategy=in-process
   ```

5. Upgrade annotation processors in this order:
   - Dagger
   - Room
   - Glide compiler
6. Decide whether to migrate Room from `kapt` to `ksp`.
7. Validate private `ru.nobird.*` and `StoriesKit` package versions.
8. If Firebase BoM is upgraded, migrate Firebase KTX usages.
9. Restore usual build commands after toolchain versions are aligned and stable.
10. Run verification:

    ```bash
    ./gradlew :model:compileDebugKotlin
    ./gradlew :billing:compileDebugKotlin
    ./gradlew :app:compileDebugKotlin
    ./gradlew :app:testDebugUnitTest
    ./gradlew :app:assembleDebug
    ```

## Open Questions

- Which Kotlin target is final: strict `1.9.25`, or a Kotlin version officially
  aligned with AGP `8.6.1`?
- Are newer private `ru.nobird.*`, `StoriesKit`, and ktlint-rule artifacts
  available in GitHub Packages?
- Should Room move to KSP during this migration, or stay on kapt until Kotlin
  `1.9.25` is stable?
- Should Firebase BoM be upgraded in the same PR, or deferred to avoid mixing
  Firebase KTX removal with the Kotlin compiler upgrade?
