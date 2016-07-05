package org.stepic.droid.view.fragments

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.squareup.otto.Subscribe
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.events.loading.FinishLoadEvent
import org.stepic.droid.events.loading.StartLoadEvent
import org.stepic.droid.events.video.FailToMoveFilesEvent
import org.stepic.droid.util.*
import org.stepic.droid.view.custom.MovingProgressDialogFragment
import org.stepic.droid.view.dialogs.ChooseStorageDialog
import org.stepic.droid.view.dialogs.ClearVideosDialog

class StoreManagementFragment : FragmentBase() {
    companion object {
        fun newInstance(): Fragment {
            val fragment = StoreManagementFragment()
            return fragment
        }
    }

    lateinit var clearCacheButton: View
    lateinit var clearCacheLabel: TextView
    private var mClearCacheDialogFragment: DialogFragment? = null
    private var loadingProgressDialog: DialogFragment? = null

    private lateinit var notMountExplanation: View
    private lateinit var mountExplanation: View
    private lateinit var chooseStorageButton: View
    private lateinit var userStorageInfo: TextView

    private var kb: String? = null
    private var mb: String? = null
    private var gb: String? = null
    private var empty: String? = null
    val loadingTag = "loading_storemanagement"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_space_management, container, false)
        return v
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.let {
            initResStrings()
            initClearCacheFeature(it)
            initAccordingToStoreState(it)
        }
        loadingProgressDialog = MovingProgressDialogFragment.newInstance()
        bus.register(this)
    }

    private fun initAccordingToStoreState(view: View) {
        notMountExplanation = view.findViewById(R.id.notMountExplanation)
        mountExplanation = view.findViewById(R.id.mountExplanation)
        chooseStorageButton = view.findViewById(R.id.choose_storage_button)
        userStorageInfo = view.findViewById(R.id.user_storage_info) as TextView

        fun hideAllStorageInfo() {
            notMountExplanation.visibility = View.GONE
            mountExplanation.visibility = View.GONE
            chooseStorageButton.visibility = View.GONE
        }

        val storageState = StorageUtil.getSDState(context)
        if (storageState == null) {
            hideAllStorageInfo()
        } else {
            if (storageState == StorageUtil.SDState.sdcardMounted) {
                notMountExplanation.visibility = View.GONE
                mountExplanation.visibility = View.VISIBLE
                chooseStorageButton.visibility = View.VISIBLE
                val chooseStorageDialog = ChooseStorageDialog()
                chooseStorageButton.setOnClickListener {
                    if (!chooseStorageDialog.isAdded) {
                        chooseStorageDialog.show(fragmentManager, null)
                    }
                }
                //TODO: ADD user_storage_info from user prefs IN userStorageInfo!
            } else if (storageState == StorageUtil.SDState.sdCardNotMounted) {
                notMountExplanation.visibility = View.VISIBLE
                mountExplanation.visibility = View.GONE
                chooseStorageButton.visibility = View.GONE
            } else {
                //restricted and not available
                hideAllStorageInfo()
            }

        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {

        super.onStop()
    }

    override fun onDestroyView() {
        bus.unregister(this)
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
        clearCacheLabel = v.findViewById(R.id.clear_cache_label) as TextView
        mClearCacheDialogFragment = ClearVideosDialog()
        setUpClearCacheButton()
    }

    @Subscribe
    fun onStartLoading(event: StartLoadEvent) {
        ProgressHelper.activate(loadingProgressDialog, fragmentManager, loadingTag)
    }

    @Subscribe
    fun onFinishLoading(event: FinishLoadEvent) {
        setUpClearCacheButton()
        ProgressHelper.dismiss(fragmentManager , loadingTag)
    }

    private fun setUpClearCacheButton() {
        clearCacheButton.setOnClickListener {
            YandexMetrica.reportEvent(Analytic.Interaction.METRICA_CLICK_CLEAR_CACHE)
            mClearCacheDialogFragment?.show(fragmentManager, null)
        }

        val clearCacheStringBuilder = StringBuilder()
        var size = FileUtil.getFileOrFolderSizeInKb(mUserPreferences.userDownloadFolder)
        size += FileUtil.getFileOrFolderSizeInKb(mUserPreferences.sdCardDownloadFolder)
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


    @Subscribe
    fun onStorageMovedFail(event: FailToMoveFilesEvent) {
        ProgressHelper.dismiss(fragmentManager, loadingTag)
        context?.let {
            Toast.makeText(context, R.string.fail_move, Toast.LENGTH_SHORT).show()
        }
    }
}