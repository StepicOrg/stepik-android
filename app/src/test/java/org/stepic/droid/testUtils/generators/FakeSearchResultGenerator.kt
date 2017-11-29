package org.stepic.droid.testUtils.generators

import org.stepic.droid.model.SearchResult

object FakeSearchResultGenerator {
    @JvmOverloads
    fun generate(courseId: Long = 0): SearchResult {
        return SearchResult(course = courseId,
                courseCover = "",
                id = "",
                score = "",
                courseOwner = "",
                courseSlug = "",
                courseTitle = "")
    }
}