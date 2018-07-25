package org.stepik.android.model.code

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.*

data class CodeOptions(
        val limits: Map<String, CodeLimit>,
        @SerializedName("execution_time_limit")
        val executionTimeLimit: Int,
        @SerializedName("code_templates")
        val codeTemplates: Map<String, String>,
        @SerializedName("execution_memory_limit")
        val executionMemoryLimit: Int,
        val samples: List<ParcelableStringList>
) : Parcelable {
    private constructor(parcel: Parcel) : this(
            parcel.readMapCustomString(CodeLimit::class.java.classLoader),
            parcel.readInt(),

            parcel.readMap(Parcel::readString, Parcel::readString),

            parcel.readInt(),
            mutableListOf<ParcelableStringList>().apply { parcel.readTypedList(this, ParcelableStringList) }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeMapCustomString(limits, flags)
        parcel.writeInt(executionTimeLimit)

        parcel.writeMap(codeTemplates, Parcel::writeString, Parcel::writeString)

        parcel.writeInt(executionMemoryLimit)
        parcel.writeTypedList(samples)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CodeOptions> {
        override fun createFromParcel(parcel: Parcel): CodeOptions = CodeOptions(parcel)
        override fun newArray(size: Int): Array<CodeOptions?> = arrayOfNulls(size)
    }
}
