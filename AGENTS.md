# Stepik Android Agent Guide

Last reviewed: 2026-04-28.

## Project Overview

Stepik Android is the open-source native Android client for Stepik, an online
learning platform. The app is distributed on Google Play and the README keeps
store links, screenshots, build/coverage badges, and Apache 2.0 license notes.

- Application id: `org.stepic.droid`.
- Main application class: `app/src/main/java/org/stepic/droid/base/App.kt`.
- Main manifest: `app/src/main/AndroidManifest.xml`.
- The codebase is mostly Kotlin with some Java and has both legacy `org.stepic.droid`
  packages and newer `org.stepik.android` layered packages.
- Build configuration is centralized in `dependencies.gradle`; root `build.gradle`
  wires repositories, Gradle plugins, and shared resolution rules.

## Gradle Modules

The project is declared in `settings.gradle`:

- `:app` - main Android application module. Owns UI, DI graph, networking,
  persistence, resources, assets, manifests, build types, and release packaging.
- `:model` - Android library with shared Stepik model/DTO classes, including
  many `Parcelable` models used across app features.
- `:billing` - Android library around Google Play Billing. Contains billing
  data/domain/remote layers and a Dagger billing component used by `:app`.
- `:androidsvg` - vendored AndroidSVG library module under `com.caverock.androidsvg`.

## Build Stack

Important versions live in `dependencies.gradle`.

- Gradle wrapper: `7.2`.
- Android Gradle Plugin: `7.1.2`.
- Kotlin: `1.7.10`.
- SDKs: `minSdk 21`, `targetSdk 34`, `compileSdk 34`, build tools `34.0.0`.
- Java/Kotlin target: JVM 1.8.
- AndroidX is enabled in `gradle.properties`.
- `:app` uses multidex, ViewBinding, and the deprecated
  `kotlin-android-extensions` plugin. Synthetic view imports and old
  `kotlinx.android.parcel` usage still exist, so Kotlin upgrades need care.
- Repositories include Google, Maven Central, JCenter, JitPack, flatDir AARs,
  and private GitHub Packages for `AndroidKit`, `StoriesKit`, and ktlint rules.
  Private package access may need `GITHUB_USER` and `GITHUB_PERSONAL_ACCESS_TOKEN`.

## Build Types

Declared in `app/build.gradle`:

- `debug` - debug signing, `-debug` suffix, debug endpoint/config support.
- `release` - production signing, minify/shrink enabled, Crashlytics native
  symbol upload configured.
- `releaseOldKeys` - release-like build signed with old keys and
  `BuildConfig.IS_GOOGLE_PLAY=false`.
- `stage` - release-like build signed with old production keys.
- `stageDebuggable` - debuggable variant based on `stage`, with debug endpoint
  support and `-debuggable` suffix.

## Root Structure

Important directories:

```text
.
|-- androidsvg/              # Vendored AndroidSVG library module
|-- app/                     # Main Android application module
|   |-- libs/                # Local AAR/JAR artifacts
|   |-- src/
|   |   |-- main/
|   |   |   |-- AndroidManifest.xml
|   |   |   |-- java/        # App source root
|   |   |   |   |-- org/stepic/droid/   # Legacy app infrastructure and older features
|   |   |   |   `-- org/stepik/android/ # Layered feature code: remote/cache/data/domain/presentation/view
|   |   |   |-- res/         # Layouts, themes, strings, drawables, XML
|   |   |   `-- assets/      # Web, KaTeX, CSS/JS, images, animations, configs
|   |   |-- debug/           # Debug-only code, endpoint switching, assets
|   |   |-- stageDebuggable/ # Stage-debuggable-only code/assets
|   |   |-- release/         # Release build-type implementations
|   |   |-- stage/           # Stage build-type implementations
|   |   |-- releaseOldKeys/  # Old-key release implementations
|   |   |-- test/            # JVM unit tests/resources
|   |   |-- androidTest/     # Instrumented tests
|   |   `-- sharedTest/      # Sources shared by unit and android tests
|   `-- build.gradle         # App build types, plugins, dependencies
|-- billing/                 # Google Play Billing library module
|-- code_quality_tools/      # ktlint, Checkstyle, PMD, Jacoco, FindBugs scripts
|-- docs/                    # Project notes and migration plans
|-- fastlane/                # Google Play alpha deployment lane
|-- gradle/                  # Gradle wrapper files
|-- model/                   # Shared Stepik model/DTO library module
|-- reports/                 # Generated local reports
|-- screenshots/             # README/store screenshots
|-- build.gradle             # Root Gradle build and repositories
|-- dependencies.gradle      # Central versions and dependency coordinates
|-- gradle.properties        # AndroidX/Kotlin/Gradle-wide settings
`-- settings.gradle          # Module list
```

## App Manifest Summary

`app/src/main/AndroidManifest.xml` defines the app shell and external entry points.

- Application class: `.base.App`.
- Launcher entry: `activity-alias org.stepic.droid.view.activities.SplashActivity`
  targeting `org.stepic.droid.ui.activities.SplashActivity`.
- Main navigation: `org.stepic.droid.ui.activities.MainFeedActivity`.
- Auth callbacks: `org.stepik.android.view.auth.ui.activity.SocialAuthActivity`
  handles the `stepic://.../oauth` flow.
- Deep links/App Links cover Stepik hosts for catalog, notifications, stories,
  courses, lessons/steps, users, and review sessions.
- Branch links use the `stepik://open` scheme and `https://stepik.app.link`.
- Video playback has a dedicated `VideoPlayerActivity` and foreground media
  playback service with PiP support.
- FCM is handled by `.notifications.StepicFcmListenerService`.
- Other manifest components include download receivers/services, notification
  alarm/boot services, content/file providers, analytics provider, and Samsung
  multi-window metadata.
- Permissions include network, downloads, calendar, foreground service/media
  playback, vibration, boot completed, billing, and post notifications.

## Architecture Map

There are two major organization styles:

- Legacy `org.stepic.droid` contains older app infrastructure, base classes,
  DI, analytics, persistence, preferences, notifications, adaptive flow, and
  legacy UI/util packages.
- Newer `org.stepik.android` is organized by layer and feature:
  `remote`, `cache`, `data`, `domain`, `presentation`, `view`.

For a typical feature, search the feature name across these layers:

- `remote/<feature>` - Retrofit services, response models, remote data sources.
- `cache/<feature>` - cache models, DAOs, Room/SQLite-backed data sources.
- `data/<feature>` - repository implementations and data source contracts.
- `domain/<feature>` - interactors, repository interfaces, models, mappers,
  analytics contracts.
- `presentation/<feature>` - presenters, ViewModels, Redux features, reducers,
  action dispatchers, and presentation state.
- `view/<feature>` - Activities, Fragments, adapters, delegates, routing,
  dialogs, and Android UI glue.
- `view/injection/<feature>` - Dagger modules/components for feature wiring.

Common presentation styles are MVP (`PresenterBase` + `*View`) and Nobird Redux
(`Feature`, `StateReducer`, `RxActionDispatcher`, ViewModel wrappers).

## Dependency Injection

Dagger 2 is the DI framework.

- Root graph: `app/src/main/java/org/stepic/droid/di/AppCoreComponent.kt`.
- Core bindings/providers: `AppCoreModule.kt`, `ConfigModule.kt`,
  `FirebaseModule.kt`, `GoogleModule.kt`, `NotificationModule.kt`,
  `RemoteMessageHandlersModule.kt`, and analytics/storage modules.
- Storage graph: `di/storage/StorageComponent.kt` and `StorageModule.kt`.
- Billing graph: `billing/src/main/java/org/stepik/android/view/injection/billing`.
- Feature graphs usually live under `org.stepik.android.view.injection.<feature>`.

When adding a feature dependency, first look for the matching `*DataModule`,
feature `*Component`, and `AppCoreComponent` builder/exposure pattern.

## Data, Network, and Persistence

- Network stack: Retrofit 2, OkHttp, Gson, RxJava 2 adapters.
- Base network helpers live under `org.stepik.android.remote.base`.
- Runtime config is loaded from bundled JSON assets via `ConfigImpl`; debug and
  stage-debuggable builds can switch endpoint configs through debug source sets.
- Do not copy values from asset configs into docs, logs, or issue comments.
- Persistence is mixed:
  - Newer Room database: `AppDatabase` in `org.stepik.android.cache.base.database`.
  - Current `AppDatabase.VERSION` is `81`; migrations live in
    `org.stepic.droid.storage.migration`.
  - Legacy DAO/facade storage lives under `org.stepic.droid.storage` and is wired
    by `StorageModule`.
  - Analytics has a separate Room database.
- If changing persisted schema, update entities/DAOs, `AppDatabase.VERSION`, and
  add a migration in the existing migration chain.

## Major Libraries and Services

- DI: Dagger 2 with kapt.
- Async/reactive: RxJava 2, RxAndroid, RxKotlin, RxRelay.
- UI: AndroidX AppCompat/Fragment/RecyclerView/ViewPager/ViewPager2/CardView,
  Material Components, ConstraintLayout, viewbindingpropertydelegate.
- Media: ExoPlayer with media session extension.
- Images/content: Glide, Glide transformations, JSoup, AndroidSVG.
- Analytics/crash/reporting: Firebase Analytics, Firebase Messaging, Firebase
  Remote Config, Firebase Crashlytics/NDK, Amplitude, AppMetrica, Branch.
- Auth/social: Google Sign-In, VK SDK.
- Billing: Google Play Billing Client 6.
- Debug/tooling: TooLargeTool in debug/stageDebuggable; Flipper is currently
  no-op in non-debug variants per comments in `app/build.gradle`.

## Resources and UI

- XML layouts live in `app/src/main/res/layout*`.
- Themes/styles are mainly in `values/themes.xml`, `values/styles.xml`, and
  `values-night`.
- Strings are in `values/strings.xml` and localized Russian resources in
  `values-ru`.
- Code/highlighting resources are in `values/*code_theme.xml` and assets.
- Web/LaTeX rendering assets are in `app/src/main/assets`.

## Tests and Quality Commands

Useful local commands:

- `./gradlew :app:assembleDebug`
- `./gradlew :app:lintDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:ktlint`
- `./gradlew :app:ktlintFormat`
- `./gradlew :app:pmd`
- `./gradlew :app:checkstyle`

Notes:

- `app/build.gradle` adds `src/sharedTest/java` to both unit and android tests.
- Existing test dependencies include JUnit, Robolectric, Mockito, Hamcrest,
  AndroidX test, Espresso, and Kaspresso.
- `lintOptions.abortOnError=false`, so lint reports can contain existing issues
  without failing the build.
- Jacoco tasks are generated per build type after project evaluation.

## Agent Working Notes

- Prefer `rg` / `rg --files` for navigation.
- Preserve the existing package/layer style of the feature you are touching.
- Do not move legacy code into the newer layered structure unless the task asks
  for a migration.
- Keep module boundaries: shared models in `:model`, billing logic in `:billing`,
  app UI/integration in `:app`, AndroidSVG changes in `:androidsvg`.
- Be careful with `buildsystem/secret.gradle`, keystores, and asset config values;
  treat them as sensitive even when present in the repository.
- Before changing externally reachable manifest components, review Android
  `exported`, App Links, OAuth, Branch, FCM, and notification behavior.