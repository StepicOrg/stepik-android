package org.stepic.droid.web.storage.model

import com.google.gson.annotations.SerializedName

class StorageRequest(
        @SerializedName("storage-record") val record: StorageRecordWrapped
)