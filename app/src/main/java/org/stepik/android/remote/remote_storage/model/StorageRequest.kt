package org.stepik.android.remote.remote_storage.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.web.storage.model.StorageRecordWrapped

class StorageRequest(
    @SerializedName("storage-record") val record: StorageRecordWrapped
)