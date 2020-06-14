package org.stepik.android.cache.step.structure

object DbStructureStep {
    const val TABLE_NAME = "step"

    object Column {
        const val ID = "id"
        const val LESSON_ID = "lesson_id"
        const val POSITION = "position"

        const val STATUS = "status"
        const val PROGRESS = "progress"

        const val SUBSCRIPTION = "subscription"

        const val VIEWED_BY = "viewed_by"
        const val PASSED_BY = "passed_by"
        const val WORTH = "worth"

        const val CREATE_DATE = "create_date"
        const val UPDATE_DATE = "update_date"

        const val DISCUSSION_COUNT = "discussion_count"
        const val DISCUSSION_PROXY = "discussion_proxy"
        const val DISCUSSION_THREADS = "discussion_threads"
        const val PEER_REVIEW = "has_peer_review"
        const val HAS_SUBMISSION_RESTRICTION = "has_submission_restriction"
        const val MAX_SUBMISSION_COUNT = "max_submission_count"
        const val CORRECT_RATIO = "correct_ratio"
    }
}