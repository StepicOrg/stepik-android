package org.stepic.droid.web.storage.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.Meta
import org.stepic.droid.model.StorageRecord

class StorageResponse(
        val meta: Meta?,
        @SerializedName("storage-records") val records: List<StorageRecord>
)