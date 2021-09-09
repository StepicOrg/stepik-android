package org.stepik.android.domain.user_reviews.model

data class UserCourseReviewsResult(
    val userCourseReviewItems: List<UserCourseReviewItem>,
    val potentialHeader: List<UserCourseReviewItem.PotentialReviewHeader>,
    val potentialReviewItems: List<UserCourseReviewItem.PotentialReviewItem>,
    val reviewedHeader: List<UserCourseReviewItem.ReviewedHeader>,
    val reviewedReviewItems: List<UserCourseReviewItem.ReviewedItem>
)