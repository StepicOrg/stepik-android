package org.stepic.droid.web.storage.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class StorageRecord<T>(
        val id: Long? = null,
        val user: Long? = null,
        val kind: String,
        val data: T,
        @SerializedName("create_date") val createDate: String? = null,
        @SerializedName("update_date") val updateDate: String? = null
) {
    fun wrap(gson: Gson = Gson()) = StorageRecordWrapped(
            id, user, kind, gson.toJsonTree(data), createDate, updateDate
    )
}