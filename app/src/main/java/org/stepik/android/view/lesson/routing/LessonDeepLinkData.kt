package org.stepik.android.view.lesson.routing

data class LessonDeepLinkData(
    val lessonId: Long,
    val stepPosition: Long = 1,
    val unitId: Long?,
    val discussionId: Long?
)