package org.stepic.droid.web.storage.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta

class StorageResponse(
        val meta: Meta?,
        @SerializedName("storage-records") val records: List<StorageRecordWrapped>
)