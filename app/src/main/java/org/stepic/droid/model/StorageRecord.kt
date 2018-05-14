package org.stepic.droid.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

class StorageRecord(
        val id: Long? = null,
        val user: Long? = null,
        val kind: String,
        val data: JsonElement,
        @SerializedName("create_date") val createDate: String? = null,
        @SerializedName("update_date") val updateDate: String? = null
)