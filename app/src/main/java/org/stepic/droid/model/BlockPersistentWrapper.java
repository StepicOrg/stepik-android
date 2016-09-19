package org.stepic.droid.model;

public class BlockPersistentWrapper {
    Block block;
    long stepId;

    public BlockPersistentWrapper() {
    }

    public BlockPersistentWrapper(Block block, long stepId) {
        this.block = block;
        this.stepId = stepId;
    }

    public Block getBlock() {
        return block;
    }

    public long getStepId() {
        return stepId;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setStepId(long stepId) {
        this.stepId = stepId;
    }
}
