package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.StoreManagementPresenter
import org.stepic.droid.core.presenters.contracts.StoreManagementView
import org.stepic.droid.databinding.FragmentSpaceManagementBinding
import org.stepic.droid.persistence.model.StorageLocation
import org.stepic.droid.ui.dialogs.ChooseStorageDialog
import org.stepic.droid.ui.dialogs.ClearVideosDialog
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.MovingProgressDialogFragment
import org.stepic.droid.ui.dialogs.WantMoveDataDialog
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.TextUtil
import org.stepik.android.view.settings.mapper.StorageLocationDescriptionMapper
import javax.inject.Inject

class StoreManagementFragment : Fragment(R.layout.fragment_space_management), StoreManagementView {
    companion object {
        private const val LOADING_TAG = "loading_store_management"

        fun newInstance(): Fragment =
            StoreManagementFragment()
    }

    private val storeManagementBinding: FragmentSpaceManagementBinding by viewBinding(FragmentSpaceManagementBinding::bind)

    private var mClearCacheDialogFragment: DialogFragment? = null
    private var loadingProgressDialogFragment: DialogFragment? = null

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var storeManagementPresenter: StoreManagementPresenter

    @Inject
    internal lateinit var storageLocationDescriptionMapper: StorageLocationDescriptionMapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    private fun injectComponent() {
        App.component().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        storeManagementBinding.clearCacheButton.setOnClickListener(null)
        storeManagementBinding.chooseStorageButton.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun hideAllStorageInfo() {
        storeManagementBinding.notMountExplanation.visibility = View.GONE
        storeManagementBinding.mountExplanation.visibility = View.GONE
        storeManagementBinding.chooseStorageButton.visibility = View.GONE
    }

    override fun setStorageOptions(options: List<StorageLocation>, selectedOption: StorageLocation?) {
        when {
            options.size > 1 -> {
                storeManagementBinding.notMountExplanation.visibility = View.GONE
                storeManagementBinding.mountExplanation.visibility = View.VISIBLE
                storeManagementBinding.chooseStorageButton.visibility = View.VISIBLE
                val chooseStorageDialog = ChooseStorageDialog.newInstance()
                chooseStorageDialog.setTargetFragment(this, 0)
                storeManagementBinding.chooseStorageButton.setOnClickListener {
                    if (!chooseStorageDialog.isAdded) {
                        chooseStorageDialog.show(requireFragmentManager(), null)
                    }
                }

                storeManagementBinding.userStorageInfo.isVisible = selectedOption != null
                if (selectedOption != null) {
                    storeManagementBinding.userStorageInfo.text =
                        storageLocationDescriptionMapper.mapToDescription(options.indexOf(selectedOption), selectedOption)
                }
            }

            options.size == 1 -> {
                storeManagementBinding.notMountExplanation.visibility = View.VISIBLE
                storeManagementBinding.mountExplanation.visibility = View.GONE
                storeManagementBinding.chooseStorageButton.visibility = View.GONE
            }

            else ->
                hideAllStorageInfo()
        }
    }

    private fun initClearCacheFeature() {
        mClearCacheDialogFragment = ClearVideosDialog.newInstance()
        mClearCacheDialogFragment?.setTargetFragment(this, ClearVideosDialog.REQUEST_CODE)

        storeManagementBinding.clearCacheButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_CLEAR_CACHE)

            if (mClearCacheDialogFragment?.isAdded != true) {
                mClearCacheDialogFragment?.show(requireFragmentManager(), ClearVideosDialog.TAG)
            }
        }
        storeManagementBinding.clearCacheButton.isEnabled = false
    }

    override fun setUpClearCacheButton(cacheSize: Long) {
        if (cacheSize > 0) {
            storeManagementBinding.clearCacheButton.isEnabled = true
            storeManagementBinding.clearCacheLabel.text = TextUtil.formatBytes(cacheSize)
        } else {
            storeManagementBinding.clearCacheButton.isEnabled = false
            storeManagementBinding.clearCacheLabel.setText(R.string.empty)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == ClearVideosDialog.REQUEST_CODE && resultCode == Activity.RESULT_OK ->
                storeManagementPresenter.removeAllDownloads()

            requestCode == WantMoveDataDialog.REQUEST_CODE && resultCode == Activity.RESULT_OK ->
                data?.getParcelableExtra<StorageLocation>(WantMoveDataDialog.EXTRA_LOCATION)
                    ?.let(storeManagementPresenter::changeStorageLocation)

            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
