package org.stepik.android.domain.user_courses.interactor

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import java.util.Date
import javax.inject.Inject

class UserCoursesInteractor
@Inject
constructor(
    private val profileRepository: ProfileRepository,
    private val userCoursesRepository: UserCoursesRepository,

    @UserCoursesOperationBus
    private val userCoursesOperationPublisher: PublishSubject<UserCourse>
) {
    fun saveUserCourse(userCourse: UserCourse): Single<UserCourse> =
        if (userCourse.id == 0L) {
            userCoursesRepository
                .getUserCourseByCourseId(userCourse.course, sourceType = DataSourceType.REMOTE)
                .flatMapSingle { remoteUserCourse ->
                    savePublishUserCourse(userCourse.copy(id = remoteUserCourse.id, lastViewed = remoteUserCourse.lastViewed))
                }
        } else {
            savePublishUserCourse(userCourse)
        }

    fun addUserCourse(courseId: Long): Completable =
        profileRepository.getProfile().flatMapCompletable { profile ->
            userCoursesRepository.addUserCourse(
                UserCourse(
                    id = 0,
                    user = profile.id,
                    course = courseId,
                    isFavorite = false,
                    isPinned = false,
                    isArchived = false,
                    lastViewed = Date(DateTimeHelper.nowUtc())
                )
            )
        }

    fun removeUserCourse(courseId: Long): Completable =
        userCoursesRepository.removeUserCourse(courseId)

    private fun savePublishUserCourse(userCourse: UserCourse): Single<UserCourse> =
        userCoursesRepository
            .saveUserCourse(userCourse)
            .doOnSuccess(userCoursesOperationPublisher::onNext)
}