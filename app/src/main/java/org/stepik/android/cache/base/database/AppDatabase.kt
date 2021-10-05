package org.stepik.android.cache.base.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.stepik.android.cache.base.mapper.CollectionConverter
import org.stepik.android.cache.base.mapper.DateConverter
import org.stepik.android.cache.catalog.dao.CatalogBlockDao
import org.stepik.android.cache.catalog.mapper.CatalogBlockContentTypeConverter
import org.stepik.android.cache.code_preference.dao.CodePreferenceDao
import org.stepik.android.cache.review_instruction.dao.ReviewInstructionDao
import org.stepik.android.cache.review_instruction.mapper.ReviewStrategyTypeConverter
import org.stepik.android.cache.review_session.dao.ReviewSessionDao
import org.stepik.android.cache.story.dao.StoryReactionDao
import org.stepik.android.cache.story.model.StoryReactionEntity
import org.stepik.android.cache.visited_courses.dao.VisitedCourseDao
import org.stepik.android.domain.catalog.model.CatalogBlock
import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.cache.code_preference.model.CodePreference
import org.stepik.android.cache.course_recommendations.dao.CourseRecommendationsDao
import org.stepik.android.cache.exam_session.dao.ExamSessionDao
import org.stepik.android.cache.proctor_session.dao.ProctorSessionDao
import org.stepik.android.cache.rubric.dao.RubricDao
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation
import org.stepik.android.domain.exam_session.model.ExamSession
import org.stepik.android.domain.proctor_session.model.ProctorSession
import org.stepik.android.domain.rubric.model.Rubric
import org.stepik.android.domain.visited_courses.model.VisitedCourse

@Database(
    entities = [
        ReviewInstruction::class,
        ReviewSession::class,
        VisitedCourse::class,
        CatalogBlock::class,
        StoryReactionEntity::class,
        CodePreference::class,
        CourseRecommendation::class,
        Rubric::class,
        ExamSession::class,
        ProctorSession::class
    ],
    version = AppDatabase.VERSION,
    exportSchema = false
)
@TypeConverters(
    CollectionConverter::class,
    DateConverter::class,

    ReviewStrategyTypeConverter::class,
    CatalogBlockContentTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val VERSION = 73
        const val NAME = "stepic_database.db"
    }

    abstract fun reviewInstructionDao(): ReviewInstructionDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun visitedCourseDao(): VisitedCourseDao
    abstract fun catalogBlockDao(): CatalogBlockDao
    abstract fun storyReactionDao(): StoryReactionDao
    abstract fun codePreferenceDao(): CodePreferenceDao
    abstract fun courseRecommendationsDao(): CourseRecommendationsDao
    abstract fun rubricDao(): RubricDao
    abstract fun examSessionDao(): ExamSessionDao
    abstract fun proctorSessionDao(): ProctorSessionDao
}