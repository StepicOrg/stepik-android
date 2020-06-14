package org.stepik.android.presentation.profile_achievements

import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.achievement.interactor.AchievementInteractor
import org.stepik.android.domain.achievement.model.AchievementItem
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.presentation.achievement.AchievementsView
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileAchievementsPresenter
@Inject
constructor(
    private val profileDataObservable: Observable<ProfileData>,
    private val achievementInteractor: AchievementInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<AchievementsView>() {
    companion object {
        private const val KEY_ACHIEVEMENTS = "achievements"
        private const val KEY_PROFILE_ID = "profile_id"
        private const val KEY_IS_MY_PROFILE = "is_my_profile"
    }

    private var state: AchievementsView.State = AchievementsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: AchievementsView) {
        super.attachView(view)
        view.setState(state)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val achievements = savedInstanceState.getParcelableArrayList<AchievementItem>(KEY_ACHIEVEMENTS)
            ?: return

        state = AchievementsView.State.AchievementsLoaded(
            achievements,
            userId = savedInstanceState.getLong(KEY_PROFILE_ID),
            isMyProfile = savedInstanceState.getBoolean(KEY_IS_MY_PROFILE)
        )
    }

    fun showAchievementsForUser(count: Int = -1, forceUpdate: Boolean = false) {
        if (state == AchievementsView.State.Idle || (forceUpdate && state == AchievementsView.State.Error)) {
            state = AchievementsView.State.SilentLoading
            compositeDisposable += profileDataObservable
                .firstElement()
                .filter { !it.user.isOrganization && !it.user.isPrivate }
                .observeOn(mainScheduler)
                .doOnSuccess { profileData ->
                    state = AchievementsView.State.Loading(profileData.user.id, profileData.isCurrentUser)
                } // post public loading to view
                .observeOn(backgroundScheduler)
                .flatMapSingleElement { profileData ->
                    achievementInteractor
                        .getAchievements(profileData.user.id, count)
                        .map { Triple(it, profileData.user.id, profileData.isCurrentUser) }
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { (achievements, userId, isMyProfile) ->
                        state = AchievementsView.State.AchievementsLoaded(achievements, userId, isMyProfile)
                    },
                    onComplete = { state = AchievementsView.State.NoAchievements },
                    onError = { state = AchievementsView.State.Error }
                )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val oldState = (state as? AchievementsView.State.AchievementsLoaded)
            ?: return

        outState.putParcelableArrayList(KEY_ACHIEVEMENTS, ArrayList(oldState.achievements))
        outState.putLong(KEY_PROFILE_ID, oldState.userId)
        outState.putBoolean(KEY_IS_MY_PROFILE, oldState.isMyProfile)
    }
}