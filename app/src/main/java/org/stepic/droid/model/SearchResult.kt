package org.stepic.droid.model

data class SearchResult (

    var id: String?,
    var score: String?, // it is not String, but let String

    //type=course
    var course: Long = 0,
    var course_cover: String?,
    var course_owner: String? ,// it is number
    var course_title: String?,
    var course_slug: String?,

    //type=lesson
    var lesson: Long = 0,
    var lesson_title: String?,
    var lesson_slug: String?,
    var lesson_owner: String?,
    var lesson_cover_url: String?,

    //type=step
    var step: Long = 0,
    var step_position: Int = 0,

    //type=comment
    var comment: Long = 0,
    var comment_parent: Long = 0,
    var comment_user: Long = 0,
    var comment_text: String?
)
