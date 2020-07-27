package org.stepik.android.domain.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.base.analytic.AnalyticSource
import java.util.EnumSet

class TestAnalyticEvent(timeStamp: Long, language: String, courseList: String) : AnalyticEvent {
    companion object {
        private const val TIMESTAMP = "timestamp"
        private const val TAGS = "tags"
        private const val LANGUAGE = "language"
        private const val COURSE_LIST = "course_list"
    }
    override val name: String
        get() = "TestEvent"

    override val params: Map<String, Any> =
        mapOf(
            TIMESTAMP to timeStamp,
            TAGS to mapOf(LANGUAGE to language, COURSE_LIST to courseList)
        )

    override val sources: EnumSet<AnalyticSource>
        get() = EnumSet.of(AnalyticSource.STEPIK_API)
}