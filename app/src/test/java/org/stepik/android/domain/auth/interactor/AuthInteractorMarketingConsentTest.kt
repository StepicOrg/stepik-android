package org.stepik.android.domain.auth.interactor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.util.AppConstants
import org.stepik.android.data.auth.storage.PendingSocialMarketingConsentStorage
import org.stepik.android.domain.auth.model.PendingSocialMarketingConsent
import org.stepik.android.domain.auth.model.SocialAuthType
import org.stepik.android.domain.auth.repository.AuthRepository
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user_profile.repository.UserProfileRepository
import org.stepik.android.domain.visited_courses.repository.VisitedCoursesRepository
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.User
import org.stepik.android.remote.auth.model.OAuthResponse
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class AuthInteractorMarketingConsentTest {

    @Mock
    private lateinit var analytic: Analytic

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var userProfileRepository: UserProfileRepository

    @Mock
    private lateinit var profileRepository: ProfileRepository

    @Mock
    private lateinit var pendingStorage: PendingSocialMarketingConsentStorage

    @Mock
    private lateinit var courseRepository: CourseRepository

    @Mock
    private lateinit var visitedCoursesRepository: VisitedCoursesRepository

    @Mock
    private lateinit var wishlistRepository: WishlistRepository

    private lateinit var interactor: AuthInteractor

    @Before
    fun setUp() {
        interactor = AuthInteractor(
            analytic, authRepository, userProfileRepository, profileRepository,
            pendingStorage, courseRepository, visitedCoursesRepository, wishlistRepository
        )
        whenever(courseRepository.removeCachedCourses()) doReturn Completable.complete()
        whenever(visitedCoursesRepository.removedVisitedCourses()) doReturn Completable.complete()
        whenever(wishlistRepository.removeWishlistEntries()) doReturn Completable.complete()
    }

    // AC1: New-account heuristic (pure function tests)

    @Test
    fun `AC1 user joined less than 5 minutes ago is new registration`() {
        val now = System.currentTimeMillis()
        val joinDate = Date(now - AppConstants.MILLIS_IN_1MINUTE)
        assert(AuthInteractor.isNewSocialRegistration(joinDate, now))
    }

    @Test
    fun `AC1 user joined exactly 4 minutes 59 seconds ago is new registration`() {
        val now = System.currentTimeMillis()
        val joinDate = Date(now - 4 * AppConstants.MILLIS_IN_1MINUTE - 59 * 1000)
        assert(AuthInteractor.isNewSocialRegistration(joinDate, now))
    }

    @Test
    fun `AC1 user joined exactly 5 minutes ago is existing login`() {
        val now = System.currentTimeMillis()
        val joinDate = Date(now - 5 * AppConstants.MILLIS_IN_1MINUTE)
        assert(!AuthInteractor.isNewSocialRegistration(joinDate, now))
    }

    @Test
    fun `AC1 user joined more than 5 minutes ago is existing login`() {
        val now = System.currentTimeMillis()
        val joinDate = Date(now - 10 * AppConstants.MILLIS_IN_1MINUTE)
        assert(!AuthInteractor.isNewSocialRegistration(joinDate, now))
    }

    @Test
    fun `AC1 user with null join date is existing login`() {
        assert(!AuthInteractor.isNewSocialRegistration(null, System.currentTimeMillis()))
    }

    // AC2: SUBSCRIBED updates profile with true + clears

    @Test
    fun `AC2 new registration with subscribed consent updates profile with true and clears pending`() {
        val now = System.currentTimeMillis()
        val newUser = User(joinDate = Date(now - 60_000))
        val profile = Profile(id = 1)

        whenever(authRepository.authWithCode(any())) doReturn Single.just(oAuthResponse())
        whenever(userProfileRepository.getUserProfile()) doReturn Single.just(newUser to profile)
        whenever(pendingStorage.get()) doReturn PendingSocialMarketingConsent.SUBSCRIBED
        whenever(profileRepository.saveProfile(any())) doReturn Single.just(profile)

        interactor.authWithCode("code", testAuthType).test().assertComplete()

        verify(profileRepository).saveProfile(argThat { subscribedForMarketing == true })
        verify(pendingStorage).clear()
    }

    // AC3: NOT_SUBSCRIBED updates profile with false + clears

    @Test
    fun `AC3 new registration with not subscribed consent updates profile with false and clears pending`() {
        val now = System.currentTimeMillis()
        val newUser = User(joinDate = Date(now - 60_000))
        val profile = Profile(id = 1)

        whenever(authRepository.authWithCode(any())) doReturn Single.just(oAuthResponse())
        whenever(userProfileRepository.getUserProfile()) doReturn Single.just(newUser to profile)
        whenever(pendingStorage.get()) doReturn PendingSocialMarketingConsent.NOT_SUBSCRIBED
        whenever(profileRepository.saveProfile(any())) doReturn Single.just(profile)

        interactor.authWithCode("code", testAuthType).test().assertComplete()

        verify(profileRepository).saveProfile(argThat { subscribedForMarketing == false })
        verify(pendingStorage).clear()
    }

    // AC4: NONE skips update + clears

    @Test
    fun `AC4 new registration with none consent skips profile update and clears pending`() {
        val now = System.currentTimeMillis()
        val newUser = User(joinDate = Date(now - 60_000))
        val profile = Profile(id = 1)

        whenever(authRepository.authWithCode(any())) doReturn Single.just(oAuthResponse())
        whenever(userProfileRepository.getUserProfile()) doReturn Single.just(newUser to profile)
        whenever(pendingStorage.get()) doReturn PendingSocialMarketingConsent.NONE

        interactor.authWithCode("code", testAuthType).test().assertComplete()

        verify(profileRepository, never()).saveProfile(any())
        verify(pendingStorage).clear()
    }

    // AC5: Existing login never updates + clears

    @Test
    fun `AC5 existing login with subscribed consent skips profile update and clears pending`() {
        val now = System.currentTimeMillis()
        val existingUser = User(joinDate = Date(now - 10 * AppConstants.MILLIS_IN_1MINUTE))
        val profile = Profile(id = 1)

        whenever(authRepository.authWithCode(any())) doReturn Single.just(oAuthResponse())
        whenever(userProfileRepository.getUserProfile()) doReturn Single.just(existingUser to profile)
        whenever(pendingStorage.get()) doReturn PendingSocialMarketingConsent.SUBSCRIBED

        interactor.authWithCode("code", testAuthType).test().assertComplete()

        verify(profileRepository, never()).saveProfile(any())
        verify(pendingStorage).clear()
    }

    @Test
    fun `AC5 existing login with not subscribed consent skips profile update and clears pending`() {
        val now = System.currentTimeMillis()
        val existingUser = User(joinDate = Date(now - 10 * AppConstants.MILLIS_IN_1MINUTE))
        val profile = Profile(id = 1)

        whenever(authRepository.authWithCode(any())) doReturn Single.just(oAuthResponse())
        whenever(userProfileRepository.getUserProfile()) doReturn Single.just(existingUser to profile)
        whenever(pendingStorage.get()) doReturn PendingSocialMarketingConsent.NOT_SUBSCRIBED

        interactor.authWithCode("code", testAuthType).test().assertComplete()

        verify(profileRepository, never()).saveProfile(any())
        verify(pendingStorage).clear()
    }

    // AC6: Profile update failure is best-effort

    @Test
    fun `AC6 profile save failure is swallowed, pending cleared, auth completes`() {
        val now = System.currentTimeMillis()
        val newUser = User(joinDate = Date(now - 60_000))
        val profile = Profile(id = 1)

        whenever(authRepository.authWithCode(any())) doReturn Single.just(oAuthResponse())
        whenever(userProfileRepository.getUserProfile()) doReturn Single.just(newUser to profile)
        whenever(pendingStorage.get()) doReturn PendingSocialMarketingConsent.SUBSCRIBED
        whenever(profileRepository.saveProfile(any())) doReturn Single.error(RuntimeException("save failed"))

        interactor.authWithCode("code", testAuthType).test().assertComplete()

        verify(profileRepository).saveProfile(any())
        verify(pendingStorage).clear()
    }

    // AC7: Endpoint failure leaves pending untouched

    @Test
    fun `AC7 authWithCode endpoint failure does not touch pending consent`() {
        whenever(authRepository.authWithCode(any())) doReturn Single.error(RuntimeException("auth failed"))

        interactor.authWithCode("code", testAuthType).test().assertError(RuntimeException::class.java)

        verify(pendingStorage, never()).get()
        verify(pendingStorage, never()).clear()
        verify(profileRepository, never()).saveProfile(any())
        verify(analytic, never()).reportAmplitudeEvent(any(), any())
    }

    // AC8: Analytics behavior preserved

    @Test
    fun `AC8 new social registration reports REGISTERED analytics event`() {
        val now = System.currentTimeMillis()
        val newUser = User(joinDate = Date(now - 60_000))
        val profile = Profile(id = 1)

        whenever(authRepository.authWithCode(any())) doReturn Single.just(oAuthResponse())
        whenever(userProfileRepository.getUserProfile()) doReturn Single.just(newUser to profile)
        whenever(pendingStorage.get()) doReturn PendingSocialMarketingConsent.NONE

        interactor.authWithCode("code", testAuthType).test().assertComplete()

        verify(analytic).reportAmplitudeEvent(
            eq(AmplitudeAnalytic.Auth.REGISTERED),
            eq(mapOf(AmplitudeAnalytic.Auth.PARAM_SOURCE to testAuthType.identifier))
        )
    }

    @Test
    fun `AC8 existing social login reports LOGGED_ID analytics event`() {
        val now = System.currentTimeMillis()
        val existingUser = User(joinDate = Date(now - 10 * AppConstants.MILLIS_IN_1MINUTE))
        val profile = Profile(id = 1)

        whenever(authRepository.authWithCode(any())) doReturn Single.just(oAuthResponse())
        whenever(userProfileRepository.getUserProfile()) doReturn Single.just(existingUser to profile)
        whenever(pendingStorage.get()) doReturn PendingSocialMarketingConsent.NONE

        interactor.authWithCode("code", testAuthType).test().assertComplete()

        verify(analytic).reportAmplitudeEvent(
            eq(AmplitudeAnalytic.Auth.LOGGED_ID),
            eq(mapOf(AmplitudeAnalytic.Auth.PARAM_SOURCE to testAuthType.identifier))
        )
    }

    @Test
    fun `AC8 profile load failure falls back to LOGGED_ID analytics event`() {
        whenever(authRepository.authWithCode(any())) doReturn Single.just(oAuthResponse())
        whenever(userProfileRepository.getUserProfile()) doReturn Single.error(RuntimeException("profile load failed"))
        whenever(pendingStorage.get()) doReturn PendingSocialMarketingConsent.NONE

        interactor.authWithCode("code", testAuthType).test().assertComplete()

        verify(analytic).reportAmplitudeEvent(
            eq(AmplitudeAnalytic.Auth.LOGGED_ID),
            eq(mapOf(AmplitudeAnalytic.Auth.PARAM_SOURCE to testAuthType.identifier))
        )
    }

    companion object {
        private val testAuthType = object : SocialAuthType {
            override val identifier = "test_provider"
            override val isNeedUseAccessTokenInsteadOfCode = false
        }

        private fun oAuthResponse() = OAuthResponse(
            refreshToken = "refresh",
            expiresIn = 3600,
            accessToken = "access",
            tokenType = "bearer"
        )
    }
}
