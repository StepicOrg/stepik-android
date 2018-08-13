package org.stepic.droid.persistence.model

import java.io.File

class StorageLocation(
        val path: File,
        val freeSpaceBytes: Long = path.freeSpace,
        val totalSpaceBytes: Long = path.totalSpace,
        val type: Type
) {
    enum class Type {
        APP_INTERNAL, // app's internal dir Context::getFilesDir, for this dir type relative paths should be used
        PRIMARY, // default external dir
        SECONDARY // additional external dirs
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StorageLocation

        if (type == Type.APP_INTERNAL && other == Type.APP_INTERNAL) return true
        if (path.canonicalPath != other.path.canonicalPath) return false

        return true
    }

    override fun hashCode(): Int = path.canonicalPath.hashCode()
}