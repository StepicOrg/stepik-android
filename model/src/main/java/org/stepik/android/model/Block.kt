package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

import org.stepik.android.model.code.CodeOptions

//more fields look at stepik.org/api/steps/14671
data class Block(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("video")
    var video: Video? = null, //always external video

    @SerializedName("options")
    val options: CodeOptions? = null,

    /**
     * ignored fields only for step source
     */
    @SerializedName("subtitle_files")
    val subtitleFiles: JsonElement? = null,

    @SerializedName("subtitles")
    val subtitles: JsonElement? = null,

    @SerializedName("source")
    val source: JsonElement? = null,

    @SerializedName("tests_archive")
    val testsArchive: JsonElement? = null,

    @SerializedName("feedback_correct")
    val feedbackCorrect: JsonElement? = null,

    @SerializedName("feedback_wrong")
    val feedbackWrong: JsonElement? = null
) : Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.name)
        dest.writeString(this.text)
        dest.writeParcelable(this.video, flags)
        dest.writeParcelable(this.options, flags)
    }

    companion object CREATOR: Parcelable.Creator<Block> {
        override fun createFromParcel(parcel: Parcel): Block =
            Block(
                parcel.readString(),
                parcel.readString(),
                parcel.readParcelable(Video::class.java.classLoader),
                parcel.readParcelable(CodeOptions::class.java.classLoader)
            )

        override fun newArray(size: Int): Array<Block?> =
            arrayOfNulls(size)
    }
}


