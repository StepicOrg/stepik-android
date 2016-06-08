package org.stepic.droid.model

data class StorageOption(
        val presentableInfo: String? = null,
        val isChosen: Boolean = false,
        val total: Long = 0, //in byte
        val free: Long = 0 //in byte
)
