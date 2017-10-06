package org.stepic.droid.model.code

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

enum class ProgrammingLanguage : Parcelable {

    @SerializedName("python3")
    PYTHON,
    @SerializedName("c++11")
    CPP11,
    @SerializedName("c++")
    CPP,
    @SerializedName("c")
    C,
    @SerializedName("haskell")
    HASKELL,
    @SerializedName("haskell 7.10")
    HASKELL7,
    @SerializedName("haskell 8.0")
    HASKELL8,
    @SerializedName("java")
    JAVA,
    @SerializedName("java8")
    JAVA8,
    @SerializedName("octave")
    OCTAVE,
    @SerializedName("asm32")
    ASM32,
    @SerializedName("asm64")
    ASM64,
    @SerializedName("shell")
    SHELL,
    @SerializedName("rust")
    RUST,
    @SerializedName("r")
    R,
    @SerializedName("ruby")
    RUBY,
    @SerializedName("clojure")
    CLOJURE,
    @SerializedName("mono c#")
    CS,
    @SerializedName("javascript")
    JAVASCRIPT,
    @SerializedName("scala")
    SCALA,
    @SerializedName("kotlin")
    KOTLIN,
    @SerializedName("go")
    GO,
    @SerializedName("pascalabc")
    PASCAL,
    @SerializedName("perl")
    PERL;

    override fun describeContents(): Int = 0


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    companion object CREATOR : Parcelable.Creator<ProgrammingLanguage> {

        override fun createFromParcel(parcel: Parcel): ProgrammingLanguage =
                ProgrammingLanguage.values()[parcel.readInt()]

        override fun newArray(size: Int): Array<ProgrammingLanguage?> = arrayOfNulls(size)

    }

}
