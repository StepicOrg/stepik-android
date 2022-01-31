package org.stepik.android.domain.course_news.interactor

import io.reactivex.Observable
import io.reactivex.Single
import org.stepik.android.domain.announcement.repository.AnnouncementRepository
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_news.model.CourseNewsListItem
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseNewsInteractor
@Inject
constructor(
    private val courseObservableSource: Observable<Course>,
    private val announcementRepository: AnnouncementRepository
) {
    fun getCourse(): Observable<Course> =
        courseObservableSource

    fun getAnnouncements(announcementIds: List<Long>, sourceType: DataSourceType): Single<List<CourseNewsListItem.Data>> =
        announcementRepository
            .getAnnouncements(announcementIds, sourceType)
            .map { announcements -> announcements.map(CourseNewsListItem::Data) }
}