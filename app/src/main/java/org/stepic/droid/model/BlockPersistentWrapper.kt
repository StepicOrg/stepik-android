package org.stepic.droid.model

import org.stepik.android.model.structure.Block

data class BlockPersistentWrapper(
        val block: Block,
        val stepId: Long
)