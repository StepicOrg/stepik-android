package org.stepic.droid.features.stories.storage.dao

import android.content.ContentValues
import android.database.Cursor
import org.stepic.droid.di.storage.StorageSingleton
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.features.stories.storage.structure.DbStructureViewedStoryTemplates
import org.stepic.droid.storage.dao.DaoBase
import org.stepic.droid.storage.operations.DatabaseOperations
import javax.inject.Inject

@StorageSingleton
class ViewedStoryTemplatesDaoImpl
@Inject
constructor(databaseOperations: DatabaseOperations): DaoBase<ViewedStoryTemplate>(databaseOperations) {
    override fun getDbName(): String =
            DbStructureViewedStoryTemplates.VIEWED_STORY_TEMPLATES

    override fun getDefaultPrimaryColumn(): String =
            DbStructureViewedStoryTemplates.Columns.ID

    override fun getDefaultPrimaryValue(persistentObject: ViewedStoryTemplate): String =
            persistentObject.storyTemplateId.toString()

    override fun getContentValues(persistentObject: ViewedStoryTemplate): ContentValues = ContentValues(1).apply {
        put(DbStructureViewedStoryTemplates.Columns.ID, persistentObject.storyTemplateId)
    }

    override fun parsePersistentObject(cursor: Cursor): ViewedStoryTemplate =
            ViewedStoryTemplate(cursor.getLong(cursor.getColumnIndex(DbStructureViewedStoryTemplates.Columns.ID)))

}