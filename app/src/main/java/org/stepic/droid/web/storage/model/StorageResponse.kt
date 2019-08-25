package org.stepic.droid.web.storage.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class StorageResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("storage-records")
    val records: List<StorageRecordWrapped>
) : MetaResponse