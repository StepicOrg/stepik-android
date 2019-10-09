package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.DialogFragment
import androidx.core.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_space_management.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.StoreManagementPresenter
import org.stepic.droid.core.presenters.contracts.StoreManagementView
import org.stepic.droid.persistence.model.StorageLocation
import org.stepic.droid.ui.dialogs.*
import org.stepic.droid.util.*
import javax.inject.Inject

class StoreManagementFragment : FragmentBase(), StoreManagementView {
    companion object {
        fun newInstance(): Fragment = StoreManagementFragment()

        private const val LOADING_TAG = "loading_store_management"
    }

    private var mClearCacheDialogFragment: DialogFragment? = null
    private var loadingProgressDialogFragment: DialogFragment? = null

    @Inject
    lateinit var storeManagementPresenter: StoreManagementPresenter

    override fun injectComponent() {
        App.component().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_space_management, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nullifyActivityBackground()
        initClearCacheFeature()
        hideAllStorageInfo()
    }

    override fun onStart() {
        super.onStart()
        storeManagementPresenter.attachView(this)
    }

    override fun onStop() {
        storeManagementPresenter.detachView(this)
        super.onStop()
    }

    override fun onDestroyView() {
        clearCacheButton.setOnClickListener(null)
        chooseStorageButton.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun hideAllStorageInfo() {
        notMountExplanation.visibility = View.GONE
        mountExplanation.visibility = View.GONE
        chooseStorageButton.visibility = View.GONE
    }

    override fun setStorageOptions(options: List<StorageLocation>) {
        when {
            options.size > 1 -> {
                notMountExplanation.visibility = View.GONE
                mountExplanation.visibility = View.VISIBLE
                chooseStorageButton.visibility = View.VISIBLE
                val chooseStorageDialog = ChooseStorageDialog.newInstance()
                chooseStorageDialog.setTargetFragment(this, 0)
                chooseStorageButton.setOnClickListener {
                    if (!chooseStorageDialog.isAdded) {
                        chooseStorageDialog.show(fragmentManager, null)
                    }
                }
            }

            options.size == 1 -> {
                notMountExplanation.visibility = View.VISIBLE
                mountExplanation.visibility = View.GONE
                chooseStorageButton.visibility = View.GONE
            }

            else ->
                hideAllStorageInfo()
        }
    }

    private fun initClearCacheFeature() {
        mClearCacheDialogFragment = ClearVideosDialog.newInstance()
        mClearCacheDialogFragment?.setTargetFragment(this, ClearVideosDialog.REQUEST_CODE)

        clearCacheButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_CLEAR_CACHE)

            if (mClearCacheDialogFragment?.isAdded != true) {
                mClearCacheDialogFragment?.show(fragmentManager, ClearVideosDialog.TAG)
            }
        }
        clearCacheButton.isEnabled = false
    }

    override fun setUpClearCacheButton(cacheSize: Long) {
        if (cacheSize > 0) {
            clearCacheButton.isEnabled = true
            clearCacheLabel.text = TextUtil.formatBytes(cacheSize)
        } else {
            clearCacheButton.isEnabled = false
            clearCacheLabel.setText(R.string.empty)
        }

    }

    override fun showLoading(isMove: Boolean) {
        loadingProgressDialogFragment = if (isMove) {
            MovingProgressDialogFragment.newInstance()
        } else {
            LoadingProgressDialogFragment.newInstance()
        }
        ProgressHelper.activate(loadingProgressDialogFragment, fragmentManager, LOADING_TAG)
    }

    override fun hideLoading() {
        ProgressHelper.dismiss(fragmentManager, LOADING_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when {
        requestCode == ClearVideosDialog.REQUEST_CODE && resultCode == Activity.RESULT_OK ->
            storeManagementPresenter.removeAllDownloads()

        requestCode == WantMoveDataDialog.REQUEST_CODE && resultCode == Activity.RESULT_OK ->
            data?.getParcelableExtra<StorageLocation>(WantMoveDataDialog.EXTRA_LOCATION)
                    ?.let(storeManagementPresenter::changeStorageLocation) ?: Unit

        else ->
            super.onActivityResult(requestCode, resultCode, data)
    }
}