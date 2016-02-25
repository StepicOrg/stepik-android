package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable

import java.io.Serializable

class Lesson : Parcelable, Serializable {
    var id: Long = 0
    var steps: LongArray? = null
    //    private String actions;
    var tags: IntArray? = null
    var playlists: Array<String>? = null
    var is_featured: Boolean = false
    var is_prime: Boolean = false
    var progress: String? = null
    var owner: Int = 0
    var subscriptions: Array<String>? = null
    var viewed_by: Int = 0
    var passed_by: Int = 0
    var dependencies: Array<String>? = null
    var followers: Array<String>? = null
    var language: String? = null
    var is_public: Boolean = false
    var title: String? = null
    var slug: String? = null
    var create_date: String? = null
    var update_date: String? = null
    var learners_group: String? = null
    var teacher_group: String? = null
    var is_cached: Boolean = false
    var is_loading: Boolean = false
    var cover_url: String? = null

    constructor() {
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLongArray(this.steps)
        dest.writeIntArray(this.tags)
        dest.writeStringArray(this.playlists)
        dest.writeByte(if (is_featured) 1.toByte() else 0.toByte())
        dest.writeByte(if (is_prime) 1.toByte() else 0.toByte())
        dest.writeString(this.progress)
        dest.writeInt(this.owner)
        dest.writeStringArray(this.subscriptions)
        dest.writeInt(this.viewed_by)
        dest.writeInt(this.passed_by)
        dest.writeStringArray(this.dependencies)
        dest.writeStringArray(this.followers)
        dest.writeString(this.language)
        dest.writeByte(if (is_public) 1.toByte() else 0.toByte())
        dest.writeString(this.title)
        dest.writeString(this.slug)
        dest.writeString(this.create_date)
        dest.writeString(this.update_date)
        dest.writeString(this.learners_group)
        dest.writeString(this.teacher_group)
        dest.writeByte(if (is_cached) 1.toByte() else 0.toByte())
        dest.writeByte(if (is_loading) 1.toByte() else 0.toByte())
        dest.writeString(this.cover_url)
    }

    protected constructor(`in`: Parcel) {
        this.id = `in`.readLong()
        this.steps = `in`.createLongArray()
        this.tags = `in`.createIntArray()
        this.playlists = `in`.createStringArray()
        this.is_featured = `in`.readByte().toInt() != 0
        this.is_prime = `in`.readByte().toInt() != 0
        this.progress = `in`.readString()
        this.owner = `in`.readInt()
        this.subscriptions = `in`.createStringArray()
        this.viewed_by = `in`.readInt()
        this.passed_by = `in`.readInt()
        this.dependencies = `in`.createStringArray()
        this.followers = `in`.createStringArray()
        this.language = `in`.readString()
        this.is_public = `in`.readByte().toInt() != 0
        this.title = `in`.readString()
        this.slug = `in`.readString()
        this.create_date = `in`.readString()
        this.update_date = `in`.readString()
        this.learners_group = `in`.readString()
        this.teacher_group = `in`.readString()
        this.is_cached = `in`.readByte().toInt() != 0
        this.is_loading = `in`.readByte().toInt() != 0
        this.cover_url = `in`.readString()
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Lesson> = object : Parcelable.Creator<Lesson> {
            override fun createFromParcel(source: Parcel): Lesson {
                return Lesson(source)
            }

            override fun newArray(size: Int): Array<Lesson?> {
                return arrayOfNulls(size)
            }
        }
    }
}
