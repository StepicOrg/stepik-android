package org.stepik.android.domain.course_search.model

import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.SearchResult
import org.stepik.android.model.user.User
import ru.nobird.android.core.model.Identifiable

data class CourseSearchResult(
    val searchResult: SearchResult,
    val lesson: Lesson?,
    val progress: Progress?,
    val lessonOwner: User?,
    val commentOwner: User?
) : Identifiable<Long> {
    override val id: Long =
        searchResult.id
}
