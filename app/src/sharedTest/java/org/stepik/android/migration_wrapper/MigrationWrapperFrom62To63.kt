package org.stepik.android.migration_wrapper

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.migration.MigrationFrom62To63
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.story.model.StoryReactionEntity
import org.stepik.android.domain.story.model.StoryReaction

class MigrationWrapperFrom62To63(migration: MigrationFrom62To63) : MigrationWrapper(migration) {
    companion object {
        const val STORY_REACTION_ENTITY_TABLE_NAME = "StoryReactionEntity"

        const val STORY_ID_COLUMN = "storyId"
        const val REACTION_COLUMN = "reaction"
    }
    override fun runTest(db: SupportSQLiteDatabase) {
        generateStoryReaction(db)
    }

    private fun generateStoryReaction(db: SupportSQLiteDatabase) {
        val storyReactionEntity = StoryReactionEntity(storyId = 120L, reaction = StoryReaction.LIKE.name)
        db.insert(STORY_REACTION_ENTITY_TABLE_NAME, SQLiteDatabase.CONFLICT_NONE, getContentValues(storyReactionEntity))

        val cursor = db.query("SELECT * FROM $STORY_REACTION_ENTITY_TABLE_NAME")
        cursor.moveToFirst()
        val retrievedStoryEntity = parsePersistentObject(cursor)
        assert(storyReactionEntity == retrievedStoryEntity) { "Objects not equal" }
    }

    private fun getContentValues(storyReactionEntity: StoryReactionEntity): ContentValues {
        val values = ContentValues()
        values.put(STORY_ID_COLUMN, storyReactionEntity.storyId)
        values.put(REACTION_COLUMN, storyReactionEntity.reaction)
        return values
    }

    private fun parsePersistentObject(cursor: Cursor): StoryReactionEntity =
        StoryReactionEntity(
            storyId = cursor.getLong(STORY_ID_COLUMN),
            reaction = cursor.getString(REACTION_COLUMN)!!
        )
}