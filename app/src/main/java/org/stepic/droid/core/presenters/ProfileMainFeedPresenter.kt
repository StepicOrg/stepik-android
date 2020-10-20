package org.stepic.droid.core.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.ProfileMainFeedView
import org.stepic.droid.di.mainscreen.MainScreenScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import ru.nobird.android.domain.rx.emptyOnErrorStub
import org.stepik.android.domain.course_list.interactor.CourseListInteractor
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.email_address.repository.EmailAddressRepository
import org.stepik.android.domain.user_profile.repository.UserProfileRepository
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@MainScreenScope
class ProfileMainFeedPresenter
@Inject constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,

    private val courseListInteractor: CourseListInteractor,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val emailAddressRepository: EmailAddressRepository,
    private val userProfileRepository: UserProfileRepository,
    private val threadPoolExecutor: ThreadPoolExecutor,
    analytic: Analytic
) : PresenterWithPotentialLeak<ProfileMainFeedView>(analytic) {

    private val compositeDisposable = CompositeDisposable()

    private val isProfileFetching = AtomicBoolean(false)

    fun fetchProfile() {
        if (!isProfileFetching.compareAndSet(false, true)) {
            return
        }
        threadPoolExecutor.execute {
            try {
                val tempProfile = userProfileRepository.getUserProfile().blockingGet()?.second
                    ?: throw IllegalStateException("profile can't be null")
                logTeacherAnalytic(tempProfile.id)
                val emailIds = tempProfile.emailAddresses
                if (emailIds?.isNotEmpty() == true) {
                    try {
                        emailAddressRepository
                            .getEmailAddresses(emailIds)
                            .ignoreElement()
                            .blockingAwait()
                    } catch (exceptionEmails: Exception) {
                        //ok emails is not critical
                    }
                }
                sharedPreferenceHelper.storeProfile(tempProfile)
            } catch (exception: Exception) {
                //no internet for loading profile
            } finally {
                isProfileFetching.set(false)
            }
        }
    }

    private fun logTeacherAnalytic(userId: Long) {
        compositeDisposable += courseListInteractor
            .getAllCourses(
                CourseListQuery(
                    teacher = userId
                )
            )
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { courses ->
                    analytic.setTeachingCoursesCount(courses.size)
                },
                onError = emptyOnErrorStub
            )
    }

    override fun detachView(view: ProfileMainFeedView) {
        super.detachView(view)
        compositeDisposable.clear()
    }
}
