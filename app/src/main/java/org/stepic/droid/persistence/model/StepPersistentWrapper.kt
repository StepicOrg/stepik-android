package org.stepic.droid.persistence.model

import android.os.Parcel
import android.os.Parcelable
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Progressable
import org.stepik.android.model.Step
import org.stepik.android.model.Video

data class StepPersistentWrapper(
    val step: Step,
    val originalStep: Step = step,
    val cachedVideo: Video? = null // maybe more abstract
) : Progressable, Parcelable {
    override val progress: String?
        get() = step.progress

    val isStepCanHaveQuiz: Boolean =
        step.block?.name?.let { name ->
            name != AppConstants.TYPE_VIDEO &&
                    name != AppConstants.TYPE_TEXT
        } ?: false

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(step, flags)
        parcel.writeParcelable(originalStep, flags)
        parcel.writeParcelable(cachedVideo, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<StepPersistentWrapper> {
        override fun createFromParcel(parcel: Parcel): StepPersistentWrapper =
            StepPersistentWrapper(
                parcel.readParcelable(Step::class.java.classLoader)!!,
                parcel.readParcelable(Step::class.java.classLoader)!!,
                parcel.readParcelable(Video::class.java.classLoader)
            )

        override fun newArray(size: Int): Array<StepPersistentWrapper?> =
            arrayOfNulls(size)
    }
}