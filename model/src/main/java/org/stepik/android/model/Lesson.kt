package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import ru.nobird.android.core.model.Identifiable
import java.util.Date

@Parcelize
class Lesson(
    @SerializedName("id")
    override val id: Long = 0,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("cover_url")
    val coverUrl: String? = null,

    @SerializedName("courses")
    val courses: LongArray = longArrayOf(),

    @SerializedName("steps")
    val steps: LongArray = longArrayOf(),

    @SerializedName("actions")
    val actions: LessonActions? = null,

    @SerializedName("is_featured")
    val isFeatured: Boolean = false,
    @SerializedName("progress")
    override val progress: String? = null,
    @SerializedName("owner")
    val owner: Long = 0,
    @SerializedName("subscriptions")
    val subscriptions: Array<String>? = null,

    @SerializedName("viewed_by")
    val viewedBy: Long = 0,
    @SerializedName("passed_by")
    val passedBy: Long = 0,

    @SerializedName("vote_delta")
    val voteDelta: Long = 0,

    @SerializedName("language")
    val language: String? = null,
    @SerializedName("is_public")
    val isPublic: Boolean = false,

    @SerializedName("create_date")
    val createDate: Date? = null,
    @SerializedName("update_date")
    val updateDate: Date? = null,

    @SerializedName("learners_group")
    val learnersGroup: String? = null,
    @SerializedName("teachers_group")
    val teachersGroup: String? = null,

    @SerializedName("time_to_complete")
    val timeToComplete: Long = 0
) : Parcelable, Progressable, Identifiable<Long>