package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import com.google.gson.Gson
import com.google.gson.JsonArray
import org.stepic.droid.model.BlockPersistentWrapper
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepik.android.cache.block.structure.DbStructureBlock
import org.stepik.android.cache.video.dao.VideoDao
import org.stepik.android.model.Block
import org.stepik.android.model.Video
import org.stepik.android.model.code.CodeOptions
import javax.inject.Inject

class BlockDaoImpl
@Inject
constructor(
    databaseOperations: DatabaseOperations,
    private val gson: Gson,
    private val videoDao: VideoDao
) : DaoBase<BlockPersistentWrapper>(databaseOperations) {

    public override fun parsePersistentObject(cursor: Cursor): BlockPersistentWrapper {
        val indexName = cursor.getColumnIndex(DbStructureBlock.Column.NAME)
        val indexText = cursor.getColumnIndex(DbStructureBlock.Column.TEXT)
        val indexStep = cursor.getColumnIndex(DbStructureBlock.Column.STEP_ID)

        //now get video related info:
        val indexExternalVideoThumbnail = cursor.getColumnIndex(DbStructureBlock.Column.EXTERNAL_THUMBNAIL)
        val indexExternalVideoId = cursor.getColumnIndex(DbStructureBlock.Column.EXTERNAL_VIDEO_ID)
        val indexExternalVideoDuration = cursor.getColumnIndex(DbStructureBlock.Column.EXTERNAL_VIDEO_DURATION)

        val externalThumbnail = cursor.getString(indexExternalVideoThumbnail)
        val externalVideoId = cursor.getLong(indexExternalVideoId)
        val externalVideoDuration = cursor.getLong(indexExternalVideoDuration)

        var video: Video? = null
        if (externalThumbnail != null && externalVideoId > 0) {
            video = Video(externalVideoId, externalThumbnail, duration = externalVideoDuration)
        }

        val codeOptionsIndex = cursor.getColumnIndex(DbStructureBlock.Column.CODE_OPTIONS)
        val storedCodeOptionJson = cursor.getString(codeOptionsIndex)

        var codeOptions: CodeOptions? = null
        if (storedCodeOptionJson != null) {
            codeOptions = gson.fromJson(storedCodeOptionJson, CodeOptions::class.java)
        }

        val block = Block(
                name = cursor.getString(indexName),
                text = cursor.getString(indexText),
                video = video,
                options = codeOptions,
                subtitleFiles = JsonArray()
        )
        return BlockPersistentWrapper(block, stepId = cursor.getLong(indexStep))
    }

    public override fun getContentValues(blockWrapper: BlockPersistentWrapper): ContentValues {
        val values = ContentValues()

        values.put(DbStructureBlock.Column.STEP_ID, blockWrapper.stepId)
        values.put(DbStructureBlock.Column.NAME, blockWrapper.block.name)
        values.put(DbStructureBlock.Column.TEXT, blockWrapper.block.text)

        val externalVideo = blockWrapper.block.video
        if (externalVideo != null) {
            values.put(DbStructureBlock.Column.EXTERNAL_VIDEO_DURATION, externalVideo.duration)
            values.put(DbStructureBlock.Column.EXTERNAL_THUMBNAIL, externalVideo.thumbnail)
            values.put(DbStructureBlock.Column.EXTERNAL_VIDEO_ID, externalVideo.id)
        }

        blockWrapper.block.options?.let {
            val jsonString = gson.toJson(it, CodeOptions::class.java)
            values.put(DbStructureBlock.Column.CODE_OPTIONS, jsonString)
        }

        return values
    }

    public override fun getDbName() = DbStructureBlock.TABLE_NAME

    public override fun getDefaultPrimaryColumn() = DbStructureBlock.Column.STEP_ID

    public override fun getDefaultPrimaryValue(persistentObject: BlockPersistentWrapper) = persistentObject.stepId.toString()

    override fun populateNestedObjects(persistentObject: BlockPersistentWrapper): BlockPersistentWrapper =
        persistentObject.apply {
            block.video = videoDao.get(block.video?.id ?: -1)
        }

    override fun storeNestedObjects(persistentObject: BlockPersistentWrapper) {
        persistentObject.block.video?.let(videoDao::replace)
    }
}
