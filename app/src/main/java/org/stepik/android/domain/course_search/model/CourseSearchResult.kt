package org.stepik.android.domain.course_search.model

import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.SearchResult
import org.stepik.android.model.Section
import org.stepik.android.model.user.User
import org.stepik.android.model.Unit
import ru.nobird.android.core.model.Identifiable

data class CourseSearchResult(
    val searchResult: SearchResult,
    val lesson: Lesson? = null,
    val progress: Progress? = null,
    val unit: Unit? = null,
    val section: Section? = null,
    val commentOwner: User? = null
) : Identifiable<Long> {
    override val id: Long =
        searchResult.id
}
