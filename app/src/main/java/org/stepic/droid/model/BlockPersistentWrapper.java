package org.stepic.droid.model;

public class BlockPersistentWrapper {
    Block mBlock;
    long stepId;

    public BlockPersistentWrapper() {
    }

    public BlockPersistentWrapper(Block block, long stepId) {
        mBlock = block;
        this.stepId = stepId;
    }

    public Block getBlock() {
        return mBlock;
    }

    public long getStepId() {
        return stepId;
    }

    public void setBlock(Block block) {
        mBlock = block;
    }

    public void setStepId(long stepId) {
        this.stepId = stepId;
    }
}
