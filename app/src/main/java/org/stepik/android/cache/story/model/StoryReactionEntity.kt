package org.stepik.android.cache.story.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StoryReactionEntity(
    @PrimaryKey
    val storyId: Long,
    val reaction: String
)