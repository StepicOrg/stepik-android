package org.stepik.android.domain.visited_courses.interactor

import io.reactivex.Completable
import org.stepik.android.domain.visited_courses.repository.VisitedCoursesRepository
import javax.inject.Inject

class VisitedCoursesInteractor
@Inject
constructor(
    private val visitedCoursesRepository: VisitedCoursesRepository
) {
    fun saveVisitedCourse(courseId: Long): Completable =
        visitedCoursesRepository.saveVisitedCourse(courseId)
}