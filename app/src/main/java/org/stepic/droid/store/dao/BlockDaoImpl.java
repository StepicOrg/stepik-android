package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Block;
import org.stepic.droid.model.BlockPersistentWrapper;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.store.structure.DbStructureBlock;
import org.stepic.droid.store.structure.DbStructureCachedVideo;

import java.util.ArrayList;
import java.util.List;

public class BlockDaoImpl extends DaoBase<BlockPersistentWrapper> {
    private final IDao<CachedVideo> videoDao;

    public BlockDaoImpl(SQLiteDatabase openHelper, IDao<CachedVideo> videoDao) {
        super(openHelper);
        this.videoDao = videoDao;
    }

    @Override
    public BlockPersistentWrapper parsePersistentObject(Cursor cursor) {
        BlockPersistentWrapper blockPersistentWrapper = new BlockPersistentWrapper();

        int indexName = cursor.getColumnIndex(DbStructureBlock.Column.NAME);
        int indexText = cursor.getColumnIndex(DbStructureBlock.Column.TEXT);
        int indexStep = cursor.getColumnIndex(DbStructureBlock.Column.STEP_ID);

        Block block = new Block();
        block.setName(cursor.getString(indexName));
        block.setText(cursor.getString(indexText));

        blockPersistentWrapper.setBlock(block);
        blockPersistentWrapper.setStepId(cursor.getLong(indexStep));

        return blockPersistentWrapper;
    }

    @Override
    public ContentValues getContentValues(BlockPersistentWrapper blockWrapper) {
        ContentValues values = new ContentValues();
        if (blockWrapper.getBlock() == null) return values;

        values.put(DbStructureBlock.Column.STEP_ID, blockWrapper.getStepId());
        values.put(DbStructureBlock.Column.NAME, blockWrapper.getBlock().getName());
        values.put(DbStructureBlock.Column.TEXT, blockWrapper.getBlock().getText());

        return values;
    }

    @Override
    public String getDbName() {
        return DbStructureBlock.BLOCKS;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureBlock.Column.STEP_ID;
    }

    @Override
    public String getDefaultPrimaryValue(BlockPersistentWrapper persistentObject) {
        return persistentObject.getStepId()+"";
    }

    @Nullable
    @Override
    public BlockPersistentWrapper get(String whereColumn, String whereValue) {
        BlockPersistentWrapper blockWrapper = super.get(whereColumn, whereValue);
        addVideoToBlockWrapper(blockWrapper);
        return blockWrapper;
    }

    @Override
    protected List<BlockPersistentWrapper> getAllWithQuery(String query, String[] whereArgs) {
        List<BlockPersistentWrapper> blockWrapperList = super.getAllWithQuery(query, whereArgs);
        for (BlockPersistentWrapper blockWrapperItem : blockWrapperList) {
            addVideoToBlockWrapper(blockWrapperItem);
        }
        return blockWrapperList;
    }

    private void addVideoToBlockWrapper(BlockPersistentWrapper blockWrapper){
        if (blockWrapper != null && blockWrapper.getBlock() != null) {
            CachedVideo video = videoDao.get(DbStructureCachedVideo.Column.STEP_ID, blockWrapper.getStepId() + "");
            blockWrapper.getBlock().setVideo(transformCachedVideoToRealVideo(video));
        }
    }

    //// FIXME: 17.02.16 refactor this hack
    @Nullable
    private Video transformCachedVideoToRealVideo(CachedVideo video) {
        Video realVideo = null;
        if (video != null) {
            realVideo = new Video();
            realVideo.setId(video.getVideoId());
            realVideo.setThumbnail(video.getThumbnail());
            VideoUrl videoUrl = new VideoUrl();
            videoUrl.setQuality(video.getQuality());
            videoUrl.setUrl(video.getUrl());

            List<VideoUrl> list = new ArrayList<>();
            list.add(videoUrl);
            realVideo.setUrls(list);
        }
        return realVideo;
    }
}
