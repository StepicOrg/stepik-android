package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.persistence.model.StorageLocation

interface StoreManagementView {
    fun setStorageOptions(options: List<StorageLocation>)
    fun setUpClearCacheButton(cacheSize: Long)

    fun showLoading()
    fun hideLoading()
}