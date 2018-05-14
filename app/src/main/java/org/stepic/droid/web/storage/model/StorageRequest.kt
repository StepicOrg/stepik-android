package org.stepic.droid.web.storage.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.StorageRecord

class StorageRequest(
        @SerializedName("storage-record") val record: StorageRecord
)