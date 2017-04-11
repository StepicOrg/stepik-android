package org.stepic.droid.test_utils.generators

import org.stepic.droid.model.SearchResult

object FakeSearchResultGenerator {
    @JvmOverloads
    fun generate(courseId: Long = 0): SearchResult {
        return SearchResult(course = courseId,
                comment_text = "",
                course_cover = "",
                id = "",
                score = "",
                course_owner = "",
                course_slug = "",
                course_title = "",
                lesson = 0,
                lesson_cover_url = "",
                lesson_slug = "",
                lesson_owner = "",
                lesson_title = "")
    }
}