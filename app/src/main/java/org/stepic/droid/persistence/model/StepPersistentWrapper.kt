package org.stepic.droid.persistence.model

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Progressable
import org.stepik.android.model.Step
import org.stepik.android.model.Video
import ru.nobird.android.core.model.Identifiable

@Parcelize
data class StepPersistentWrapper(
    val step: Step,
    val originalStep: Step = step,
    val cachedVideo: Video? = null // maybe more abstract
) : Progressable, Parcelable, Identifiable<Long> {
    @IgnoredOnParcel
    override val id: Long =
        step.id

    @IgnoredOnParcel
    override val progress: String?
        get() = step.progress

    @IgnoredOnParcel
    val isStepCanHaveQuiz: Boolean =
        step.block?.name?.let { name ->
            name != AppConstants.TYPE_VIDEO &&
                    name != AppConstants.TYPE_TEXT
        } ?: false
}