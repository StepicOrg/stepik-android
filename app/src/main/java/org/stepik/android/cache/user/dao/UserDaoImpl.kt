package org.stepik.android.cache.user.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getBoolean
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.user.structure.DbStructureUser
import org.stepik.android.model.user.User
import java.util.*
import javax.inject.Inject

class UserDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<User>(databaseOperations) {
    override fun getDbName(): String =
        DbStructureUser.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        DbStructureUser.Columns.ID

    override fun getDefaultPrimaryValue(persistentObject: User): String =
        persistentObject.id.toString()

    override fun getContentValues(user: User): ContentValues {
        val values = ContentValues()

        values.put(DbStructureUser.Columns.ID, user.id)
        values.put(DbStructureUser.Columns.PROFILE, user.profile)
        values.put(DbStructureUser.Columns.FIRST_NAME, user.firstName)
        values.put(DbStructureUser.Columns.LAST_NAME, user.lastName)
        values.put(DbStructureUser.Columns.FULL_NAME, user.fullName)
        values.put(DbStructureUser.Columns.SHORT_BIO, user.shortBio)
        values.put(DbStructureUser.Columns.DETAILS, user.details)
        values.put(DbStructureUser.Columns.AVATAR, user.avatar)
        values.put(DbStructureUser.Columns.IS_PRIVATE, user.isPrivate)
        values.put(DbStructureUser.Columns.IS_ORGANIZATION, user.isOrganization)
        values.put(DbStructureUser.Columns.JOIN_DATE, user.joinDate?.time ?: -1)

        return values
    }

    override fun parsePersistentObject(cursor: Cursor): User =
        User(
            id          = cursor.getLong(DbStructureUser.Columns.ID),
            profile     = cursor.getLong(DbStructureUser.Columns.PROFILE),
            firstName   = cursor.getString(DbStructureUser.Columns.FIRST_NAME),
            lastName    = cursor.getString(DbStructureUser.Columns.LAST_NAME),
            fullName    = cursor.getString(DbStructureUser.Columns.FULL_NAME),
            shortBio    = cursor.getString(DbStructureUser.Columns.SHORT_BIO),
            details     = cursor.getString(DbStructureUser.Columns.DETAILS),
            avatar      = cursor.getString(DbStructureUser.Columns.AVATAR),
            isPrivate   = cursor.getBoolean(DbStructureUser.Columns.IS_PRIVATE),
            isOrganization = cursor.getBoolean(DbStructureUser.Columns.IS_ORGANIZATION),
            joinDate    = cursor.getLong(DbStructureUser.Columns.JOIN_DATE).takeIf { it >= 0 }?.let(::Date)
        )


}