package org.stepik.android.domain.user_courses.interactor

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.view.injection.course_list.UserCoursesOperationBus
import javax.inject.Inject

class UserCoursesInteractor
@Inject
constructor(
    private val userCoursesRepository: UserCoursesRepository,

    @UserCoursesOperationBus
    private val userCoursesOperationPublisher: PublishSubject<UserCourse>
) {
    fun saveUserCourse(userCourse: UserCourse): Single<UserCourse> =
        userCoursesRepository
            .saveUserCourse(userCourse)
            .doOnSuccess(userCoursesOperationPublisher::onNext)
}