@file:Suppress("DEPRECATION")

package org.stepic.droid.model.code

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Deprecated("Use string literal instead. This class is only for knowledge what programming languages are existed")
enum class ProgrammingLanguage(val serverPrintableName: String) : Parcelable {

    @SerializedName("python3")
    PYTHON("python3"),
    @SerializedName("c++11")
    CPP11("c++11"),
    @SerializedName("c++")
    CPP("c++"),
    @SerializedName("c")
    C("c"),
    @SerializedName("haskell")
    HASKELL("haskell"),
    @SerializedName("haskell 7.10")
    HASKELL7("haskell 7.10"),
    @SerializedName("haskell 8.0")
    HASKELL8("haskell 8.0"),
    @SerializedName("java")
    JAVA("java"),
    @SerializedName("java8")
    JAVA8("java8"),
    @SerializedName("octave")
    OCTAVE("octave"),
    @SerializedName("asm32")
    ASM32("asm32"),
    @SerializedName("asm64")
    ASM64("asm64"),
    @SerializedName("shell")
    SHELL("shell"),
    @SerializedName("rust")
    RUST("rust"),
    @SerializedName("r")
    R("r"),
    @SerializedName("ruby")
    RUBY("ruby"),
    @SerializedName("clojure")
    CLOJURE("clojure"),
    @SerializedName("mono c#")
    CS("mono c#"),
    @SerializedName("javascript")
    JAVASCRIPT("javascript"),
    @SerializedName("scala")
    SCALA("scala"),
    @SerializedName("kotlin")
    KOTLIN("kotlin"),
    @SerializedName("go")
    GO("go"),
    @SerializedName("pascalabc")
    PASCAL("pascalabc"),
    @SerializedName("perl")
    PERL("perl");

    override fun describeContents(): Int = 0


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    companion object CREATOR : Parcelable.Creator<ProgrammingLanguage> {

        override fun createFromParcel(parcel: Parcel): ProgrammingLanguage =
                ProgrammingLanguage.values()[parcel.readInt()]

        override fun newArray(size: Int): Array<ProgrammingLanguage?> = arrayOfNulls(size)

        //make it public and resolve highlighting
        @Suppress("unused")
        private fun highlighting(serverName: String) {
            val language = serverNameToLanguage(serverName)
            when (language) {
                ProgrammingLanguage.PYTHON -> TODO()
                ProgrammingLanguage.CPP11 -> TODO()
                ProgrammingLanguage.CPP -> TODO()
                ProgrammingLanguage.C -> TODO()
                ProgrammingLanguage.HASKELL -> TODO()
                ProgrammingLanguage.HASKELL7 -> TODO()
                ProgrammingLanguage.HASKELL8 -> TODO()
                ProgrammingLanguage.JAVA -> TODO()
                ProgrammingLanguage.JAVA8 -> TODO()
                ProgrammingLanguage.OCTAVE -> TODO()
                ProgrammingLanguage.ASM32 -> TODO()
                ProgrammingLanguage.ASM64 -> TODO()
                ProgrammingLanguage.SHELL -> TODO()
                ProgrammingLanguage.RUST -> TODO()
                ProgrammingLanguage.R -> TODO()
                ProgrammingLanguage.RUBY -> TODO()
                ProgrammingLanguage.CLOJURE -> TODO()
                ProgrammingLanguage.CS -> TODO()
                ProgrammingLanguage.JAVASCRIPT -> TODO()
                ProgrammingLanguage.SCALA -> TODO()
                ProgrammingLanguage.KOTLIN -> TODO()
                ProgrammingLanguage.GO -> TODO()
                ProgrammingLanguage.PASCAL -> TODO()
                ProgrammingLanguage.PERL -> TODO()
                null -> TODO()
            }

        }

        private fun serverNameToLanguage(serverName: String): ProgrammingLanguage? {
            return values()
                    .find {
                        it.serverPrintableName.equals(serverName, ignoreCase = true)
                    } ?: return null
        }

    }

}
