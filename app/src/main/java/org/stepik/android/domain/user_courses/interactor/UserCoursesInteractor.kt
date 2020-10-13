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
        userCoursesRepository
            .saveUserCourse(userCourse)
            .doOnSuccess(userCoursesOperationPublisher::onNext)

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

    fun getUserCourseByCourseId(courseId: Long): Single<UserCourse> =
        userCoursesRepository
            .getUserCourseByCourseId(courseId, sourceType = DataSourceType.REMOTE)
            .toSingle()
}