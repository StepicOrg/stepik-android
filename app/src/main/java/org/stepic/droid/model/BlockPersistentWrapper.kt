package org.stepic.droid.model

import org.stepik.android.model.Block

data class BlockPersistentWrapper(
        val block: Block,
        val stepId: Long
)