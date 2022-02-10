package org.stepik.android.domain.course_news.interactor

import io.reactivex.Observable
import io.reactivex.Single
import org.stepik.android.domain.announcement.model.Announcement
import org.stepik.android.domain.announcement.repository.AnnouncementRepository
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_news.model.CourseNewsListItem
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.model.Course
import java.util.Date
import javax.inject.Inject
import kotlin.Comparator

class CourseNewsInteractor
@Inject
constructor(
    private val courseObservableSource: Observable<Course>,
    private val announcementRepository: AnnouncementRepository,
    private val profileRepository: ProfileRepository
) {
    private val shortNameRegex = "\\{\\{\\s*user_name\\s*\\}\\}".toRegex()
    private val fullNameRegex = "\\{\\{\\s*user_full_name\\s*\\}\\}".toRegex()

    fun getCourse(): Observable<Course> =
        courseObservableSource

    fun getAnnouncements(announcementIds: List<Long>, isTeacher: Boolean, sourceType: DataSourceType): Single<List<CourseNewsListItem.Data>> =
        profileRepository
            .getProfile(primarySourceType = DataSourceType.CACHE)
            .flatMap { profile ->
                announcementRepository
                    .getAnnouncements(announcementIds, sourceType)
                    .map { announcements ->
                        announcements
                            .sortedWith(getAnnouncementsComparator(isTeacher))
                            .map { announcement ->
                            CourseNewsListItem.Data(
                                announcement = announcement.copy(text =
                                    announcement
                                        .text
                                        .replace(shortNameRegex, profile.firstName?.trim().orEmpty())
                                        .replace(fullNameRegex, profile.fullName.orEmpty())
                                )
                            )
                        }
                    }
            }

    private fun getAnnouncementsComparator(isTeacher: Boolean): Comparator<Announcement> =
        if (isTeacher) {
            compareByDescending<Announcement> { it.createDate ?: Date() }.thenBy { it.status.ordinal }
        } else {
            compareByDescending<Announcement> { it.sentDate ?: Date() }
        }
}