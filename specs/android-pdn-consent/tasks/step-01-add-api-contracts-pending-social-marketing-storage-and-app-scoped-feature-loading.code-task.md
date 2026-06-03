---
status: completed
created: 2026-05-28
started: 2026-05-28
completed: 2026-05-28
---
# Task: Step 1 - Add API Contracts, Pending Social Marketing Storage, and App-Scoped Feature Loading

## Description
Add the shared data contracts, pending social marketing consent storage, and app-scoped backend feature loading needed by the Android PDn consent feature. This task prepares the e-mail and social registration flows for later UI changes without changing current consent UI behavior.

## Background
The Android PDn consent feature requires account creation flows to know whether backend feature `AuthMarketingAgreement` is enabled and to carry optional marketing subscription state. E-mail registration will later send `subscribed_for_marketing` directly during account creation. Social registration will later capture a pending local marketing choice before OAuth and apply it only after Android determines that social auth created a new account.

Backend features are currently owned by the catalog feature graph and fetched without an app-level cache. This task moves feature data ownership to the app graph, adds an in-memory full-list cache with shared in-flight requests, and preloads features from splash without blocking startup routing.

## Reference Documentation
**Required:**
- Design: `.agents/planning/2026-05-26-android-pdn-consent/design/detailed-design.md`
- Implementation Plan: `.agents/planning/2026-05-26-android-pdn-consent/implementation/plan.md`

**Additional References (if relevant to this task):**
- Feature loading research: `.agents/planning/2026-05-26-android-pdn-consent/research/feature-flag-loading.md`
- DI graph research: `.agents/planning/2026-05-26-android-pdn-consent/research/di-graph.md`

**Code References:**
- Registration payload model: `model/src/main/java/org/stepik/android/model/user/RegistrationCredentials.kt`
- Profile model: `model/src/main/java/org/stepik/android/model/user/Profile.kt`
- Backend feature models: `app/src/main/java/org/stepik/android/domain/feature/model/Feature.kt`, `app/src/main/java/org/stepik/android/domain/feature/model/Parameters.kt`
- Feature repository contract and implementation: `app/src/main/java/org/stepik/android/domain/feature/repository/FeaturesRepository.kt`, `app/src/main/java/org/stepik/android/data/features/repository/FeaturesRepositoryImpl.kt`
- Feature interactor: `app/src/main/java/org/stepik/android/domain/feature/interactor/FeaturesInteractor.kt`
- Feature remote layer: `app/src/main/java/org/stepik/android/data/features/source/FeaturesRemoteDataSource.kt`, `app/src/main/java/org/stepik/android/remote/features/FeaturesRemoteDataSourceImpl.kt`, `app/src/main/java/org/stepik/android/remote/features/service/FeaturesService.kt`
- Feature DI module and current catalog owner: `app/src/main/java/org/stepik/android/view/injection/features/FeaturesDataModule.kt`, `app/src/main/java/org/stepik/android/view/injection/catalog/CatalogComponent.kt`
- App graph and auth graph: `app/src/main/java/org/stepic/droid/di/AppCoreComponent.kt`, `app/src/main/java/org/stepik/android/view/injection/auth/AuthComponent.kt`
- Startup preload target: `app/src/main/java/org/stepic/droid/core/presenters/SplashPresenter.kt`
- SharedPreferences pattern: `app/src/main/java/org/stepic/droid/preferences/SharedPreferenceHelper.java`, `app/src/main/java/org/stepic/droid/di/AppCoreModule.kt`

**Test References:**
- Gson test helper: `app/src/test/java/org/stepic/droid/testUtils/TestingGsonProvider.kt`
- Profile test fixture: `app/src/test/java/org/stepic/droid/testUtils/generators/FakeProfileGenerator.kt`
- Parcelable profile coverage: `model/src/test/java/org/stepik/android/model/ParcelizeTest.kt`
- Parcelable helpers: `model/src/test/java/org/stepik/android/model/util/ParcelableTester.kt`, `app/src/test/java/org/stepic/droid/testUtils/ParcelableTester.kt`

**Note:** Read the design document before beginning implementation.

## Technical Requirements
1. Add nullable `subscribedForMarketing` to `RegistrationCredentials` with `@SerializedName("subscribed_for_marketing")`.
2. Add nullable `subscribedForMarketing` to `Profile` with the same serialized name, preserving existing model and parcelable behavior.
3. Add `Parameters.isEnabled` for backend feature flags.
4. Add a Kotlin enum for pending social marketing consent with states equivalent to `NONE`, `SUBSCRIBED`, and `NOT_SUBSCRIBED`.
5. Add a focused storage class for pending social marketing consent using regular Android `SharedPreferences`, exposing `set`, `get`, and `clear`.
6. Wire the pending consent storage into DI at app/auth scope so `SocialAuthActivity` and `AuthInteractor` can later use the same implementation.
7. Extend `FeaturesRepository` with `getFeatures(forceUpdate: Boolean = false)` and `getCachedFeatures()`.
8. Update `FeaturesRepositoryImpl` to keep an app-scoped in-memory full feature-list cache and a shared in-flight `Single<List<Feature>>` using `Single.cache()`.
9. Synchronize only short local repository state operations: reading cache, reading or creating the in-flight request, writing cache, and clearing the in-flight request.
10. Extend `FeaturesInteractor` with `preloadFeatures(): Completable`, `isAuthMarketingAgreementEnabled(): Single<Boolean>`, and `isAuthMarketingAgreementEnabledCached(): Boolean`.
11. Add an app-scoped `FeaturesPreloader` that subscribes on the background scheduler, swallows errors, and owns no UI lifecycle.
12. Move `FeaturesDataModule` from `CatalogComponent` into `AppCoreComponent`, mark repository, remote, and service bindings as `@AppSingleton`, and remove the catalog-local module entry.
13. Inject `FeaturesPreloader` into `SplashPresenter` and call it before the synchronous Firebase Remote Config wait starts. Splash must not wait for backend features.
14. Keep existing auth, registration, and catalog UI behavior unchanged in this step.

## Dependencies
- Existing feature flag data flow under the `features` repository, remote data source, service, and interactor packages.
- Existing Dagger app graph rooted at `AppCoreComponent` and catalog feature graph currently installing `FeaturesDataModule`.
- Existing splash startup flow in `SplashPresenter`.
- Existing Gson and model test patterns for `RegistrationCredentials`, `Profile`, and related DTOs.
- Existing regular `SharedPreferences` access patterns used by app-level auth or storage classes.

## Implementation Approach
1. Update API/model classes first, including Gson serialized names and any constructor, copy, parcelable, or test fixture updates required by the added nullable fields.
2. Add the pending social marketing enum and storage wrapper with a stable private preference key and defensive decoding of unexpected stored values to `NONE`.
3. Update `FeaturesRepository` and `FeaturesRepositoryImpl` to support cache reads, forced refresh, and shared in-flight requests while keeping remote failure behavior non-caching.
4. Update `FeaturesInteractor` to expose preload and `AuthMarketingAgreement` helpers, mapping missing feature, missing `parameters.isEnabled`, false value, and errors to false.
5. Move feature data DI bindings to `AppCoreComponent`, add app-scoped annotations, and remove the catalog-local feature data module installation.
6. Add `FeaturesPreloader`, inject it into `SplashPresenter`, and trigger preload before Firebase Remote Config blocking work.
7. Add or update focused JVM unit tests for serialization, pending storage mapping, repository cache behavior, interactor mapping, and preload error swallowing.
8. Run `./gradlew :app:testDebugUnitTest` and `./gradlew :app:assembleDebug` after implementation.

## Acceptance Criteria

1. **Registration Marketing Field Serialization**
   - Given `RegistrationCredentials` is created with `subscribedForMarketing = true` and `false`
   - When it is serialized by the app's Gson configuration
   - Then JVM tests prove the payload contains `subscribed_for_marketing` with the matching boolean value, and null or omitted values preserve current Gson behavior when marketing consent is not supplied.

2. **Profile Marketing Field Compatibility**
   - Given a `Profile` contains `subscribedForMarketing = true`, `false`, or no supplied value
   - When it is serialized and deserialized by model tests
   - Then JVM tests prove `subscribed_for_marketing` is accepted and preserved, and any existing `Profile` parcelable/model coverage is updated for the new field.

3. **Pending Social Marketing Storage Mapping**
   - Given the pending social marketing preference is absent, set to `SUBSCRIBED`, set to `NOT_SUBSCRIBED`, cleared, or contains an unexpected stored string
   - When the storage wrapper reads the pending value
   - Then JVM tests prove it returns `NONE` for absent, cleared, and unexpected values, returns the stored enum for valid values, and never crashes on invalid data.

4. **Feature Repository Cache And Force Refresh**
   - Given `FeaturesRepositoryImpl` has an empty cache and a fake remote data source
   - When callers request features successfully, request them again without force, and request them with `forceUpdate = true`
   - Then JVM tests prove the first load performs one remote request, the second non-forced load returns the cached list without another remote request, and the forced load performs another remote request.

5. **Feature Repository Shared In-Flight Request**
   - Given multiple callers request features while the first remote request is still in flight
   - When all callers subscribe before the remote request completes
   - Then JVM tests prove the callers share one remote request, all receive the same result, and the in-flight reference is cleared after success.

6. **Feature Repository Failure Handling**
   - Given a remote feature request fails
   - When `FeaturesRepositoryImpl.getFeatures()` emits the failure
   - Then JVM tests prove the failed request does not populate the cache, clears the in-flight reference, and allows a later request to call remote again.

7. **Auth Marketing Feature Mapping**
   - Given backend features include `AuthMarketingAgreement` with `parameters.isEnabled = true`, `false`, missing parameters, missing `isEnabled`, an absent feature, an empty cache, or a repository failure
   - When `FeaturesInteractor` resolves the auth marketing flag through async and cached methods
   - Then JVM tests prove only an existing feature with `parameters.isEnabled == true` maps to true, and every missing, false, empty, or failed case maps to false.

8. **Feature Preloader Non-Blocking Behavior**
   - Given `FeaturesPreloader` is invoked during splash startup
   - When feature loading succeeds or fails
   - Then JVM tests prove it subscribes to feature loading on the background scheduler and swallows errors so startup and auth navigation are not blocked by backend feature failures.

9. **App-Scoped Feature DI**
   - Given feature data bindings have moved from `CatalogComponent` to `AppCoreComponent`
   - When the app graph is compiled
   - Then splash, auth, and catalog consumers all resolve the same app-scoped feature repository binding, and catalog rubricator injection continues to compile without a catalog-local `FeaturesDataModule`.

10. **Build And Regression Verification**
   - Given all Step 1 changes are implemented
   - When `./gradlew :app:testDebugUnitTest` and `./gradlew :app:assembleDebug` are run
   - Then both commands pass, existing auth and registration UI behavior remains unchanged, and catalog continues to call the same rubricator feature path using the shared feature cache.

## Metadata
- **Complexity**: High
- **Labels**: android, kotlin, dagger, rxjava, auth, feature-flags, shared-preferences, tests
- **Required Skills**: Kotlin, Android app architecture, Dagger 2, RxJava 2, Gson model serialization, JVM unit testing
