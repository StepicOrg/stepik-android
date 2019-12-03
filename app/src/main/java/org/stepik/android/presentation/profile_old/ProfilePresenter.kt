package org.stepik.android.presentation.profile_old

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.achievement.AchievementsView
import org.stepic.droid.model.UserViewModel
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.StepikUtil
import org.stepik.android.domain.profile.interactor.ProfileInteractorOld
import org.stepik.android.model.user.Profile
import org.stepik.android.presentation.base.PresenterBase
import timber.log.Timber
import javax.inject.Inject

class ProfilePresenter
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val profileInteractor: ProfileInteractorOld,
    private val profileObservable: Observable<Profile>,

    @MainScheduler
    private val mainScheduler: Scheduler,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler
) : PresenterBase<ProfileView>() {

    private var state: ProfileView.State = ProfileView.State.Idle
        set(value) {
            field = value
            view?.setState(state)
        }

    override fun attachView(view: ProfileView) {
        super.attachView(view)
        view.setState(state)
    }

    fun initProfile(userId: Long) {
        state = ProfileView.State.Loading
        subscribeForProfileUpdates(userId)
        val profile: Profile? = sharedPreferenceHelper.profile
        if (profile != null && (userId == 0L || profile.id == userId) && !profile.isGuest) {
            showProfileBase(profile)
        } else if (userId == 0L && (profile != null && profile.isGuest || profile == null)) {
            compositeDisposable += profileInteractor
                .fetchProfile()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onError = {
                        it.printStackTrace()
                        state = ProfileView.State.NetworkError
                    },
                    onSuccess = { showProfileBase(it) }
                )
        } else {
            compositeDisposable += profileInteractor
                .fetchProfile(userId)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onError = {
                        it.printStackTrace()
                        state = ProfileView.State.NetworkError
                    },
                    onSuccess = { user ->
                        val userViewModelLocal = UserViewModel(fullName = user.fullName ?: "",
                            imageLink = user.avatar,
                            shortBio = stringOrEmpty(user.shortBio),
                            information = stringOrEmpty((user.details)),
                            isMyProfile = false,
                            isPrivate = user.isPrivate,
                            isOrganization = user.isOrganization,
                            id = userId)
                        state = ProfileView.State.ProfileLoaded(userViewModelLocal)
                        fetchPins(userId)
                    }
                )
        }
    }

    private fun showProfileBase(profile: Profile) {
        Timber.d("showProfileBase: $profile")
        val userViewModelLocal = UserViewModel(fullName = profile.fullName ?: "${profile.firstName} ${profile.lastName}",
            imageLink = profile.avatar,
            shortBio = stringOrEmpty(profile.shortBio),
            information = stringOrEmpty(profile.details),
            isMyProfile = true,
            isPrivate = profile.isPrivate,
            isOrganization = false,
            id = profile.id)
        state = ProfileView.State.ProfileLoaded(userViewModelLocal)
        fetchPins(profile.id)
    }

    private fun stringOrEmpty(str: String?): String {
        val source = str ?: ""
        return if (source.isBlank()) "" else source
    }

    /***
     *  Achievements related
     */

    private var achievementsState: AchievementsView.State = AchievementsView.State.Idle
        set(value) {
            field = value
            setViewState(value)
        }

    fun showAchievementsForUser(userId: Long, count: Int = -1, forceUpdate: Boolean = false) {
        if (achievementsState == AchievementsView.State.Idle || (forceUpdate && achievementsState == AchievementsView.State.Error)) {
            achievementsState = AchievementsView.State.Loading
            compositeDisposable += profileInteractor.fetchAchievementsForUser(userId, count)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy({
                    achievementsState = AchievementsView.State.Error
                }) {
                    achievementsState = AchievementsView.State.AchievementsLoaded(it)
                }
        } else {
            setViewState(achievementsState)
        }
    }

    private fun setViewState(newState: AchievementsView.State) {
        when (newState) {
            is AchievementsView.State.AchievementsLoaded ->
                view?.showAchievements(newState.achievements)

            is AchievementsView.State.Loading ->
                view?.onAchievementsLoading()

            is AchievementsView.State.Error ->
                view?.onAchievementsLoadingError()
        }
    }

    /***
     *  Streak related
     */

    fun showNotificationSetting() {
        compositeDisposable += profileInteractor
            .tryShowNotificationSetting()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onSuccess = { view?.showNotificationEnabledState(it.first, it.second) }
            )
    }

    fun switchNotificationStreak(isChecked: Boolean) {
        compositeDisposable += profileInteractor
            .switchNotificationStreak(isChecked)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onComplete = { view?.hideNotificationTime(!isChecked) }
            )
    }

    fun setStreakTime(timeIntervalCode: Int) {
        compositeDisposable += profileInteractor
            .setStreakTime(timeIntervalCode)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onSuccess = { view?.setNewTimeInterval(it) }
            )
    }

    private fun fetchPins(userId: Long) {
        compositeDisposable += profileInteractor
            .fetchPins(userId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onSuccess = { pins ->
                    val currentStreakLocal = StepikUtil.getCurrentStreak(pins)
                    val maxStreakLocal = StepikUtil.getMaxStreak(pins)
                    val haveSolvedTodayLocal = pins.first() != 0L
                    view?.onStreaksLoaded(currentStreakLocal, maxStreakLocal, haveSolvedTodayLocal)
                }
            )
    }

    private fun subscribeForProfileUpdates(profileId: Long) {
        compositeDisposable += profileObservable
            .filter { profileId == 0L || it.id == profileId }
            .observeOn(backgroundScheduler)
            .subscribe(::showProfileBase)
    }
}