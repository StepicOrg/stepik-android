package org.stepik.android.cache.social_profile.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.util.getLong
import org.stepic.droid.util.getString
import org.stepik.android.cache.social_profile.structure.DbStructureSocialProfile
import org.stepik.android.model.SocialProfile
import javax.inject.Inject

class SocialProfileDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations
) : DaoBase<SocialProfile>(databaseOperations) {

    override fun getDbName(): String =
        DbStructureSocialProfile.TABLE_NAME

    override fun getDefaultPrimaryColumn(): String =
        DbStructureSocialProfile.Columns.ID

    override fun getDefaultPrimaryValue(persistentObject: SocialProfile): String =
        persistentObject.id.toString()

    override fun parsePersistentObject(cursor: Cursor): SocialProfile =
        SocialProfile(
            id = cursor.getLong(DbStructureSocialProfile.Columns.ID),
            user = cursor.getLong(DbStructureSocialProfile.Columns.USER),
            provider = cursor.getString(DbStructureSocialProfile.Columns.PROVIDER)!!,
            name = cursor.getString(DbStructureSocialProfile.Columns.NAME)!!,
            url = cursor.getString(DbStructureSocialProfile.Columns.URL)!!
        )

    override fun getContentValues(persistentObject: SocialProfile): ContentValues =
        ContentValues().apply {
            put(DbStructureSocialProfile.Columns.ID, persistentObject.id)
            put(DbStructureSocialProfile.Columns.USER, persistentObject.user)
            put(DbStructureSocialProfile.Columns.PROVIDER, persistentObject.provider)
            put(DbStructureSocialProfile.Columns.NAME, persistentObject.name)
            put(DbStructureSocialProfile.Columns.URL, persistentObject.url)
        }
}