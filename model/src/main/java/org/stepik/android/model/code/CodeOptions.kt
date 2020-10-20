package org.stepik.android.model.code

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.util.ParcelableStringList

@Parcelize
data class CodeOptions(
    @SerializedName("limits")
    val limits: Map<String, CodeLimit>,
    @SerializedName("execution_time_limit")
    val executionTimeLimit: Int,
    @SerializedName("code_templates")
    val codeTemplates: Map<String, String>,
    @SerializedName("execution_memory_limit")
    val executionMemoryLimit: Int,
    @SerializedName("samples")
    val samples: List<ParcelableStringList>,
    @SerializedName("is_run_user_code_allowed")
    val isRunUserCodeAllowed: Boolean
) : Parcelable
