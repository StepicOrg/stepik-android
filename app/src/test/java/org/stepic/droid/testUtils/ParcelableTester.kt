package org.stepic.droid.testUtils

import android.os.Parcel
import android.os.Parcelable
import java.lang.reflect.Modifier


/**
 * Class must properly implement equals()!
 */
fun <T : Parcelable> Parcelable.assertThatObjectParcelable() {
    val parcel = Parcel.obtain()

    try {
        this.writeToParcel(parcel, this.describeContents())
        parcel.setDataPosition(0)

        val creator: Parcelable.Creator<T>

        try {
            val creatorField = this.javaClass.getDeclaredField("CREATOR")

            if (!Modifier.isPublic(creatorField.modifiers)) {
                throw AssertionError(this.javaClass.simpleName + ".CREATOR " +
                        "is not public")
            }

            creatorField.isAccessible = true

            @Suppress("UNCHECKED_CAST") //it will throw exception, which we will rethrow as Assertion
            creator = creatorField.get(null) as Parcelable.Creator<T>
        } catch (e: NoSuchFieldException) {
            throw AssertionError(this.javaClass.simpleName + ".CREATOR " +
                    "public static field must be presented in the class")
        } catch (e: IllegalAccessException) {
            throw AssertionError(this.javaClass.simpleName + ".CREATOR " +
                    "is not accessible")
        } catch (e: ClassCastException) {
            throw AssertionError(this.javaClass.simpleName + ".CREATOR " +
                    "field must be of type android.os.Parcelable.Creator" +
                    "<" + this.javaClass.simpleName + ">")
        }

        val objectFromParcelable = creator.createFromParcel(parcel)

        if (this != objectFromParcelable) {
            throw AssertionError("Object before serialization is not equal to object " +
                    "after serialization!\nOne of the possible problems -> " +
                    "incorrect implementation of equals()." +
                    "\nobject before = " + this +
                    ",\nobject after = " + objectFromParcelable)
        }
    } finally {
        parcel.recycle()
    }
}