package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.model.Block;
import org.stepic.droid.model.BlockPersistentWrapper;
import org.stepic.droid.store.structure.DbStructureBlock;

public class BlockDaoImpl extends DaoBase<BlockPersistentWrapper> {
    public BlockDaoImpl(SQLiteOpenHelper openHelper) {
        super(openHelper);
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
}
