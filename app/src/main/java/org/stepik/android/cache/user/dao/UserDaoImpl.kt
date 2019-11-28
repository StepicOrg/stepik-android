package org.stepik.android.cache.user.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.DbParseHelper
import org.stepic.droid.util.getBoolean
import org.stepic.droid.util.getDate
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.user.structure.DbStructureUser
import org.stepik.android.model.user.User
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
        values.put(DbStructureUser.Columns.COVER, user.cover)
        values.put(DbStructureUser.Columns.IS_PRIVATE, user.isPrivate)
        values.put(DbStructureUser.Columns.IS_ORGANIZATION, user.isOrganization)
        values.put(DbStructureUser.Columns.SOCIAL_PROFILES, DbParseHelper.parseLongListToString(user.socialProfiles))
        values.put(DbStructureUser.Columns.KNOWLEDGE, user.knowledge)
        values.put(DbStructureUser.Columns.KNOWLEDGE_RANK, user.knowledgeRank)
        values.put(DbStructureUser.Columns.REPUTATION, user.reputation)
        values.put(DbStructureUser.Columns.REPUTATION_RANK, user.reputationRank)
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
            cover       = cursor.getString(DbStructureUser.Columns.COVER),
            isPrivate   = cursor.getBoolean(DbStructureUser.Columns.IS_PRIVATE),
            isOrganization = cursor.getBoolean(DbStructureUser.Columns.IS_ORGANIZATION),
            socialProfiles = DbParseHelper.parseStringToLongList(cursor.getString(DbStructureUser.Columns.SOCIAL_PROFILES)).orEmpty(),
            knowledge   = cursor.getLong(DbStructureUser.Columns.KNOWLEDGE),
            knowledgeRank = cursor.getLong(DbStructureUser.Columns.KNOWLEDGE_RANK),
            reputation  = cursor.getLong(DbStructureUser.Columns.REPUTATION),
            reputationRank = cursor.getLong(DbStructureUser.Columns.REPUTATION_RANK),
            joinDate    = cursor.getDate(DbStructureUser.Columns.JOIN_DATE)
        )
}