package org.stepik.android.cache.base.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.stepik.android.cache.base.mapper.CollectionConverter
import org.stepik.android.cache.base.mapper.DateConverter
import org.stepik.android.cache.catalog.dao.CatalogBlockDao
import org.stepik.android.cache.catalog.mapper.CatalogBlockContentTypeConverter
import org.stepik.android.cache.review_instruction.dao.ReviewInstructionDao
import org.stepik.android.cache.review_instruction.mapper.ReviewStrategyTypeConverter
import org.stepik.android.cache.review_session.dao.ReviewSessionDao
import org.stepik.android.cache.story.dao.StoryReactionDao
import org.stepik.android.cache.story.model.StoryReactionEntity
import org.stepik.android.cache.visited_courses.dao.VisitedCourseDao
import org.stepik.android.domain.catalog.model.CatalogBlock
import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.domain.visited_courses.model.VisitedCourse

@Database(
    entities = [
        ReviewInstruction::class,
        ReviewSession::class,
        VisitedCourse::class,
        CatalogBlock::class,
        StoryReactionEntity::class
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
        const val VERSION = 64
        const val NAME = "stepic_database.db"
    }

    abstract fun reviewInstructionDao(): ReviewInstructionDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun visitedCourseDao(): VisitedCourseDao
    abstract fun catalogBlockDao(): CatalogBlockDao
    abstract fun storyReactionDao(): StoryReactionDao
}