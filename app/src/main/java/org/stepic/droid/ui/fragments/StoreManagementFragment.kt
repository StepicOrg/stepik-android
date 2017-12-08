package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.ui.dialogs.*
import org.stepic.droid.util.FileUtil
import org.stepic.droid.util.KotlinUtil
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.StorageUtil

class StoreManagementFragment : FragmentBase(), WantMoveDataDialog.Callback, ClearVideosDialog.Callback {
    companion object {
        fun newInstance(): Fragment = StoreManagementFragment()

        private const val LOADING_TAG = "loading_store_management"
    }

    private lateinit var clearCacheButton: View
    private lateinit var clearCacheLabel: TextView
    private var mClearCacheDialogFragment: DialogFragment? = null
    private var loadingProgressDialogFragment: DialogFragment? = null

    private lateinit var notMountExplanation: View
    private lateinit var mountExplanation: View
    private lateinit var chooseStorageButton: View
    private lateinit var userStorageInfo: TextView

    private var kb: String? = null
    private var mb: String? = null
    private var gb: String? = null
    private var empty: String? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.fragment_space_management, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nullifyActivityBackground()
        view?.let {
            initResStrings()
            initClearCacheFeature(it)
            initAccordingToStoreState(it)
        }
    }

    private fun initAccordingToStoreState(view: View) {
        notMountExplanation = view.findViewById(R.id.notMountExplanation)
        mountExplanation = view.findViewById(R.id.mountExplanation)
        chooseStorageButton = view.findViewById(R.id.choose_storage_button)
        userStorageInfo = view.findViewById(R.id.user_storage_info)

        fun hideAllStorageInfo() {
            notMountExplanation.visibility = View.GONE
            mountExplanation.visibility = View.GONE
            chooseStorageButton.visibility = View.GONE
        }

        val storageState = StorageUtil.getSDState(context)
        if (storageState == null) {
            hideAllStorageInfo()
        } else {
            when (storageState) {
                StorageUtil.SDState.sdcardMounted -> {
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
                    //TODO: ADD user_storage_info from user prefs IN userStorageInfo!
                }
                StorageUtil.SDState.sdCardNotMounted -> {
                    notMountExplanation.visibility = View.VISIBLE
                    mountExplanation.visibility = View.GONE
                    chooseStorageButton.visibility = View.GONE
                }
                else -> //restricted and not available
                    hideAllStorageInfo()
            }
        }
    }

    override fun onDestroyView() {
        clearCacheButton.setOnClickListener(null)
        chooseStorageButton.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun initResStrings() {
        kb = context?.getString(R.string.kb)
        mb = context?.getString(R.string.mb)
        gb = context?.getString(R.string.gb)
        empty = context?.getString(R.string.empty)
    }

    private fun initClearCacheFeature(v: View) {
        clearCacheButton = v.findViewById(R.id.clear_cache_button)
        clearCacheLabel = v.findViewById(R.id.clear_cache_label)
        mClearCacheDialogFragment = ClearVideosDialog.newInstance()
        mClearCacheDialogFragment?.setTargetFragment(this, 0)
        setUpClearCacheButton()
    }

    private fun setUpClearCacheButton() {
        clearCacheButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_CLEAR_CACHE)

            if (mClearCacheDialogFragment?.isAdded != true) {
                mClearCacheDialogFragment?.show(fragmentManager, null)
            }
        }

        val clearCacheStringBuilder = StringBuilder()
        var size = FileUtil.getFileOrFolderSizeInKb(userPreferences.userDownloadFolder)
        size += FileUtil.getFileOrFolderSizeInKb(userPreferences.sdCardDownloadFolder)
        if (size > 0) {
            clearCacheButton.isEnabled = true
            if (size > 1024) {
                size /= 1024
                if (size > 1024) {
                    val part = size % 1024
                    size /= 1024
                    val sizeInGb: Double = size + (part.toDouble() / 1024f)
                    Double.toString()
                    clearCacheStringBuilder.append(KotlinUtil.getNiceFormatOfDouble(sizeInGb))
                    clearCacheStringBuilder.append(gb)
                } else {
                    clearCacheStringBuilder.append(size)
                    clearCacheStringBuilder.append(mb)
                }
            } else {
                clearCacheStringBuilder.append(size)
                clearCacheStringBuilder.append(kb)
            }
            clearCacheLabel.text = clearCacheStringBuilder.toString()
        } else {
            clearCacheButton.isEnabled = false
            clearCacheLabel.text = empty
        }

    }

    override fun onStartLoading(isMove: Boolean) {
        if (isMove) {
            loadingProgressDialogFragment = MovingProgressDialogFragment.newInstance()
        } else {
            loadingProgressDialogFragment = LoadingProgressDialogFragment.newInstance()
        }
        ProgressHelper.activate(loadingProgressDialogFragment, fragmentManager, LOADING_TAG)
    }

    override fun onFinishLoading() {
        setUpClearCacheButton()
        ProgressHelper.dismiss(fragmentManager, LOADING_TAG)
    }

    override fun onStartLoading() {
        onStartLoading(false)
    }

    override fun onClearAllWithoutAnimation(stepIds: LongArray?) {
        // no op
    }

    override fun onFailToMove() {
        context?.let {
            Toast.makeText(context, R.string.fail_move, Toast.LENGTH_SHORT).show()
        }
    }
}