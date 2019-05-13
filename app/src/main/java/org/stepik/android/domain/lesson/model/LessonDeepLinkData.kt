package org.stepik.android.domain.lesson.model

data class LessonDeepLinkData(
    val lessonId: Long,
    val stepPosition: Long = 1,
    val unitId: Long?,
    val discussionId: Long?
)