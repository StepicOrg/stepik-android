package org.stepik.android.domain.lesson.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

data class LessonData(
    val lesson: Lesson,
    val unit: Unit?,
    val section: Section?,
    val course: Course?,
    val stepPosition: Int = 0
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(lesson, flags)
        parcel.writeParcelable(unit, flags)
        parcel.writeParcelable(section, flags)
        parcel.writeParcelable(course, flags)
        parcel.writeInt(stepPosition)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LessonData> {
        override fun createFromParcel(parcel: Parcel): LessonData =
            LessonData(
                parcel.readParcelable(Lesson::class.java.classLoader)!!,
                parcel.readParcelable(Unit::class.java.classLoader),
                parcel.readParcelable(Section::class.java.classLoader),
                parcel.readParcelable(Course::class.java.classLoader),
                parcel.readInt()
            )

        override fun newArray(size: Int): Array<LessonData?> =
            arrayOfNulls(size)
    }
}