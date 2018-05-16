package org.stepic.droid.web.storage.model

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.stepic.droid.util.toObject

class StorageRecordWrapped(
        val id: Long? = null,
        val user: Long? = null,
        val kind: String,
        val data: JsonElement,
        @SerializedName("create_date") val createDate: String? = null,
        @SerializedName("update_date") val updateDate: String? = null
) {
    inline fun <reified T> unwrap(gson: Gson = Gson()) = StorageRecord<T>(
            id, user, kind, data.toObject(gson), createDate, updateDate
    )
}