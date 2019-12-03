package org.stepik.android.view.achievement.ui.resolver

import android.content.Context
import android.widget.ImageView
import org.stepic.droid.R
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.util.liftM2
import javax.inject.Inject

@AppSingleton
class AchievementResourceResolver
@Inject
constructor(
    private val context: Context
) {
    companion object {
        private const val KIND_STEPS_SOLVED = "steps_solved"
        private const val KIND_STEPS_SOLVED_STREAK = "steps_solved_streak"
        private const val KIND_STEPS_SOLVED_CHOICE = "steps_solved_choice"
        private const val KIND_STEPS_SOLVED_CODE = "steps_solved_code"
        private const val KIND_STEPS_SOLVED_NUMBER = "steps_solved_number"
        private const val KIND_CODE_QUIZZES_SOLVED_PYTHON = "code_quizzes_solved_python"
        private const val KIND_CODE_QUIZZES_SOLVED_CPP = "code_quizzes_solved_cpp"
        private const val KIND_CODE_QUIZZES_SOLVED_JAVA = "code_quizzes_solved_java"
        private const val KIND_ACTIVE_DAYS_STREAK = "active_days_streak"
        private const val KIND_CERTIFICATES_REGULAR_COUNT = "certificates_regular_count"
        private const val KIND_CERTIFICATES_DISTINCTION_COUNT = "certificates_distinction_count"
        private const val KIND_COURSE_REVIEWS_COUNT = "course_reviews_count"
    }

    private val kindToTitleResId = mapOf(
            KIND_STEPS_SOLVED to R.string.achievement_steps_solved_title,
            KIND_STEPS_SOLVED_STREAK to R.string.achievement_steps_solved_streak_title,
            KIND_STEPS_SOLVED_CHOICE to R.string.achievement_steps_solved_choice_title,
            KIND_STEPS_SOLVED_CODE to R.string.achievement_steps_solved_code_title,
            KIND_STEPS_SOLVED_NUMBER to R.string.achievement_steps_solved_number_title,
            KIND_CODE_QUIZZES_SOLVED_PYTHON to R.string.achievement_code_quizzes_solved_python_title,
            KIND_CODE_QUIZZES_SOLVED_CPP to R.string.achievement_code_quizzes_solved_cpp_title,
            KIND_CODE_QUIZZES_SOLVED_JAVA to R.string.achievement_code_quizzes_solved_java_title,
            KIND_ACTIVE_DAYS_STREAK to R.string.achievement_active_days_streak_title,
            KIND_CERTIFICATES_REGULAR_COUNT to R.string.achievement_certificates_regular_count_title,
            KIND_CERTIFICATES_DISTINCTION_COUNT to R.string.achievement_certificates_distinction_count_title,
            KIND_COURSE_REVIEWS_COUNT to R.string.achievement_course_reviews_count_title
    )

    private val kindToDescriptionResID = mapOf(
            KIND_STEPS_SOLVED to R.string.achievement_steps_solved_description,
            KIND_STEPS_SOLVED_STREAK to R.string.achievement_steps_solved_streak_description,
            KIND_STEPS_SOLVED_CHOICE to R.string.achievement_steps_solved_choice_description,
            KIND_STEPS_SOLVED_CODE to R.string.achievement_steps_solved_code_description,
            KIND_STEPS_SOLVED_NUMBER to R.string.achievement_steps_solved_number_description,
            KIND_CODE_QUIZZES_SOLVED_PYTHON to R.string.achievement_code_quizzes_solved_python_description,
            KIND_CODE_QUIZZES_SOLVED_CPP to R.string.achievement_code_quizzes_solved_cpp_description,
            KIND_CODE_QUIZZES_SOLVED_JAVA to R.string.achievement_code_quizzes_solved_java_description,
            KIND_ACTIVE_DAYS_STREAK to R.string.achievement_active_days_streak_description,
            KIND_CERTIFICATES_REGULAR_COUNT to R.string.achievement_certificates_regular_count_description,
            KIND_CERTIFICATES_DISTINCTION_COUNT to R.string.achievement_certificates_distinction_count_description,
            KIND_COURSE_REVIEWS_COUNT to R.string.achievement_course_reviews_count_description
    )

    private val kindToPluralResID = mapOf(
            KIND_STEPS_SOLVED to R.plurals.task,
            KIND_STEPS_SOLVED_STREAK to R.plurals.task,
            KIND_STEPS_SOLVED_CHOICE to R.plurals.task,
            KIND_STEPS_SOLVED_CODE to R.plurals.task,
            KIND_STEPS_SOLVED_NUMBER to R.plurals.task,
            KIND_CODE_QUIZZES_SOLVED_PYTHON to R.plurals.problem,
            KIND_CODE_QUIZZES_SOLVED_CPP to R.plurals.problem,
            KIND_CODE_QUIZZES_SOLVED_JAVA to R.plurals.problem,
            KIND_ACTIVE_DAYS_STREAK to R.plurals.day,
            KIND_CERTIFICATES_REGULAR_COUNT to R.plurals.certificate,
            KIND_CERTIFICATES_DISTINCTION_COUNT to R.plurals.certificate,
            KIND_COURSE_REVIEWS_COUNT to R.plurals.course_review
    )

    fun resolveTitleForKind(kind: String): String =
        context.getString(kindToTitleResId[kind] ?: R.string.achievement_unknown_title)

    fun resolveDescription(achievementFlatItem: AchievementFlatItem): String =
        kindToPluralResID[achievementFlatItem.kind].liftM2(kindToDescriptionResID[achievementFlatItem.kind]) { plural, description ->
            context.getString(description, context.resources.getQuantityString(plural, achievementFlatItem.targetScore, achievementFlatItem.targetScore))
        } ?: context.getString(R.string.achievement_unknown_description)

    fun resolveAchievementIcon(achievementFlatItem: AchievementFlatItem, targetImageView: ImageView? = null) =
        if (achievementFlatItem.isLocked || achievementFlatItem.currentLevel == 0) {
            "file:///android_asset/images/vector/achievements/ic_empty_achievement.svg"
        } else {
            "file:///android_asset/images/vector/achievements/${achievementFlatItem.kind}/${achievementFlatItem.currentLevel}.svg"
//            "${achievementFlatItem.iconId ?: ""}/${targetImageView.width}x${targetImageView.height}" // todo: update after backend support
        }
}