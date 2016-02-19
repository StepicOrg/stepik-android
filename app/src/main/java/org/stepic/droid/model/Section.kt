package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.stepic.droid.base.MainApplication
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.util.DateTimeHelper
import java.io.Serializable
import java.util.*
import javax.inject.Inject

class Section : Serializable, Parcelable {

    @Inject
    lateinit var mConfig: IConfig

    private val mFormatForView: DateTimeFormatter by lazy {
        DateTimeFormat.forPattern(mConfig.datePattern).withZone(DateTimeZone.getDefault()).withLocale(Locale.getDefault())
    }
    var id: Long = 0
    var course: Long = 0 // course id
    var units: LongArray? = null
    var position: Int = 0
    var progress: String? = null
    var title: String? = null
    var slug: String? = null
    var begin_date: String? = null
    var end_date: String? = null
    var soft_deadline: String? = null
    var hard_deadline: String? = null
    var grading_policy: String? = null
    var begin_date_source: String? = null
    var end_date_source: String? = null
    var soft_deadline_source: String? = null
    var hard_deadline_source: String? = null
    var grading_policy_source: String? = null
    var is_active: Boolean = false
    var create_date: String? = null
    var update_date: String? = null
    var is_cached: Boolean = false
    var is_loading: Boolean = false

    private var formatted_begin_date: String? = null
    private var formatted_soft_deadline: String? = null
    private var formatted_hard_deadline: String? = null

    constructor() {
        MainApplication.component(MainApplication.getAppContext()).inject(this)
    }

    val formattedBeginDate: String by lazy {
        DateTimeHelper.getPresentOfDate(begin_date, mFormatForView)
    }

    val formattedSoftDeadline: String by lazy {
        DateTimeHelper.getPresentOfDate(soft_deadline, mFormatForView)
    }

    val formattedHardDeadline: String by lazy {
        DateTimeHelper.getPresentOfDate(hard_deadline, mFormatForView)
    }


    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLong(this.course)
        dest.writeLongArray(this.units)
        dest.writeInt(this.position)
        dest.writeString(this.progress)
        dest.writeString(this.title)
        dest.writeString(this.slug)
        dest.writeString(this.begin_date)
        dest.writeString(this.end_date)
        dest.writeString(this.soft_deadline)
        dest.writeString(this.hard_deadline)
        dest.writeString(this.grading_policy)
        dest.writeString(this.begin_date_source)
        dest.writeString(this.end_date_source)
        dest.writeString(this.soft_deadline_source)
        dest.writeString(this.hard_deadline_source)
        dest.writeString(this.grading_policy_source)
        dest.writeByte(if (is_active) 1.toByte() else 0.toByte())
        dest.writeString(this.create_date)
        dest.writeString(this.update_date)
        dest.writeByte(if (is_cached) 1.toByte() else 0.toByte())
        dest.writeByte(if (is_loading) 1.toByte() else 0.toByte())
        dest.writeString(this.formatted_begin_date)
        dest.writeString(this.formatted_soft_deadline)
        dest.writeString(this.formatted_hard_deadline)
    }

    protected constructor(input: Parcel) : this() {
        this.id = input.readLong()
        this.course = input.readLong()
        this.units = input.createLongArray()
        this.position = input.readInt()
        this.progress = input.readString()
        this.title = input.readString()
        this.slug = input.readString()
        this.begin_date = input.readString()
        this.end_date = input.readString()
        this.soft_deadline = input.readString()
        this.hard_deadline = input.readString()
        this.grading_policy = input.readString()
        this.begin_date_source = input.readString()
        this.end_date_source = input.readString()
        this.soft_deadline_source = input.readString()
        this.hard_deadline_source = input.readString()
        this.grading_policy_source = input.readString()
        this.is_active = input.readByte().toInt() != 0
        this.create_date = input.readString()
        this.update_date = input.readString()
        this.is_cached = input.readByte().toInt() != 0
        this.is_loading = input.readByte().toInt() != 0
        this.formatted_begin_date = input.readString()
        this.formatted_soft_deadline = input.readString()
        this.formatted_hard_deadline = input.readString()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Section> = object : Parcelable.Creator<Section> {
            override fun createFromParcel(source: Parcel): Section {
                return Section(source)
            }

            override fun newArray(size: Int): Array<Section?> {
                return arrayOfNulls(size)
            }
        }
    }

}
