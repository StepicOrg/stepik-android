package org.stepic.droid.storage.dao

import android.content.ContentValues
import android.database.Cursor
import com.google.gson.Gson
import org.stepic.droid.mappers.toDbUrl
import org.stepic.droid.mappers.toVideoUrls
import org.stepic.droid.model.*
import org.stepic.droid.model.code.CodeOptions
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.structure.DbStructureBlock
import org.stepic.droid.storage.structure.DbStructureCachedVideo
import org.stepic.droid.storage.structure.DbStructureVideoUrl
import org.stepic.droid.util.transformToVideo
import org.stepik.android.model.structure.Video
import javax.inject.Inject

class BlockDaoImpl @Inject
constructor(
        databaseOperations: DatabaseOperations,
        private val videoDao: IDao<CachedVideo>,
        private val gson: Gson,
        private val videoUrlIDao: IDao<DbVideoUrl>
) : DaoBase<BlockPersistentWrapper>(databaseOperations) {

    public override fun parsePersistentObject(cursor: Cursor): BlockPersistentWrapper {
        val indexName = cursor.getColumnIndex(DbStructureBlock.Column.NAME)
        val indexText = cursor.getColumnIndex(DbStructureBlock.Column.TEXT)
        val indexStep = cursor.getColumnIndex(DbStructureBlock.Column.STEP_ID)

        val block = Block()
        block.name = cursor.getString(indexName)
        block.text = cursor.getString(indexText)

        val blockPersistentWrapper = BlockPersistentWrapper(block, stepId = cursor.getLong(indexStep))

        //now get video related info:
        val indexExternalVideoThumbnail = cursor.getColumnIndex(DbStructureBlock.Column.EXTERNAL_THUMBNAIL)
        val indexExternalVideoId = cursor.getColumnIndex(DbStructureBlock.Column.EXTERNAL_VIDEO_ID)
        val indexExternalVideoDuration = cursor.getColumnIndex(DbStructureBlock.Column.EXTERNAL_VIDEO_DURATION)

        val externalThumbnail = cursor.getString(indexExternalVideoThumbnail)
        val externalVideoId = cursor.getLong(indexExternalVideoId)
        val externalVideoDuration = cursor.getLong(indexExternalVideoDuration)
        if (externalThumbnail != null && externalVideoId > 0) {
            val video = Video(externalVideoId, externalThumbnail, duration = externalVideoDuration)
            block.video = video
        }

        val codeOptionsIndex = cursor.getColumnIndex(DbStructureBlock.Column.CODE_OPTIONS)
        val storedCodeOptionJson = cursor.getString(codeOptionsIndex)
        if (storedCodeOptionJson != null) {
            val codeOptions = gson.fromJson(storedCodeOptionJson, CodeOptions::class.java)
            block.options = codeOptions
        }

        return blockPersistentWrapper
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

    public override fun getDbName() = DbStructureBlock.BLOCKS

    public override fun getDefaultPrimaryColumn() = DbStructureBlock.Column.STEP_ID

    public override fun getDefaultPrimaryValue(persistentObject: BlockPersistentWrapper) = persistentObject.stepId.toString()

    override fun get(whereColumnName: String, whereValue: String): BlockPersistentWrapper? {
        val blockWrapper = super.get(whereColumnName, whereValue)
        addCachedVideoToBlockWrapper(blockWrapper)
        addExternalVideoToBlockWrapper(blockWrapper)
        return blockWrapper
    }

    override fun getAllWithQuery(query: String, whereArgs: Array<String>?): List<BlockPersistentWrapper> {
        val blockWrapperList = super.getAllWithQuery(query, whereArgs)
        for (blockWrapperItem in blockWrapperList) {
            addCachedVideoToBlockWrapper(blockWrapperItem)
            addExternalVideoToBlockWrapper(blockWrapperItem)
        }
        return blockWrapperList
    }

    private fun addCachedVideoToBlockWrapper(blockWrapper: BlockPersistentWrapper?) {
        if (blockWrapper?.block == null) {
            return
        }
        val cachedVideo = videoDao.get(DbStructureCachedVideo.Column.STEP_ID, blockWrapper.stepId.toString() + "")
        blockWrapper.block.cachedLocalVideo = cachedVideo?.transformToVideo() // not local video is saved only with stepId = -1

        val durationOfExternalVideo = blockWrapper.block.video?.duration ?: 0
        if (durationOfExternalVideo > 0) {
            //set it from external, because the cached video does not have this property
            blockWrapper.block.cachedLocalVideo?.duration = durationOfExternalVideo
        }
    }

    private fun addExternalVideoToBlockWrapper(blockWrapper: BlockPersistentWrapper?) {
        if (blockWrapper?.block == null) {
            return
        }
        val externalVideoId = blockWrapper.block.video?.id?.toString() ?: return

        val externalVideoUrls: MutableList<DbVideoUrl?> = videoUrlIDao.getAll(DbStructureVideoUrl.Column.videoId, externalVideoId)

        blockWrapper.block.video?.urls = externalVideoUrls.toVideoUrls()
    }

    override fun insertOrUpdate(persistentObject: BlockPersistentWrapper?) {
        super.insertOrUpdate(persistentObject)
        if (persistentObject == null) {
            return
        }

        //add all external video urls to database
        persistentObject.block.video?.let { externalVideo ->
            val list = ArrayList<DbVideoUrl>()
            externalVideo.urls?.forEach {
                val dbExternalUrl = it.toDbUrl(externalVideo.id)
                list.add(dbExternalUrl)
            }
            videoUrlIDao.remove(DbStructureVideoUrl.Column.videoId, externalVideo.id.toString())
            list.forEach {
                videoUrlIDao.insertOrUpdate(it)
            }
        }
    }
}
