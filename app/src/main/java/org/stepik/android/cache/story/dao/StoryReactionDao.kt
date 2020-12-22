package org.stepik.android.cache.story.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.story.model.StoryReactionEntity

@Dao
interface StoryReactionDao {
    @Query("SELECT * FROM StoryReactionEntity")
    fun getStoryReactions(): Single<List<StoryReactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveStoryReaction(storyReactionEntity: StoryReactionEntity): Completable
}