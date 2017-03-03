package org.stepic.droid.ui.dialogs

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.Shell
import org.stepic.droid.services.UpdateWithApkService
import org.stepic.droid.util.AppConstants
import javax.inject.Inject

class NeedUpdatingDialog : DialogFragment() {

    companion object {

        private val LINK_KEY = "link_key"
        private val IS_IN_GP_KEY = "is_in_gp"

        fun newInstance(link: String?, isInGooglePlay: Boolean): NeedUpdatingDialog {

            val args = Bundle()
            args.putString(LINK_KEY, link)
            args.putBoolean(IS_IN_GP_KEY, isInGooglePlay)
            val fragment = NeedUpdatingDialog()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var shell: Shell

    @Inject
    lateinit var analytic : Analytic

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        App.component().inject(this)

        val link = arguments.getString(LINK_KEY)
        val isInGP = arguments.getBoolean(IS_IN_GP_KEY)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.update_available_title).setPositiveButton(R.string.update_now) { _, _ ->
            analytic.reportEvent(Analytic.Interaction.UPDATING_MESSAGE_IS_APPROVED)
            if (isInGP) {
                shell.screenProvider.showStoreWithApp(activity)
            } else {

                val permissionCheck = ContextCompat.checkSelfPermission(App.getAppContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    shell.sharedPreferenceHelper.storeTempLink(link)

                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        val explainDialog = ExplainExternalStoragePermissionDialog.newInstance()
                        if (!explainDialog.isAdded) {
                            explainDialog.show(activity.supportFragmentManager, null)
                        }

                    } else {
                        ActivityCompat.requestPermissions(activity,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                AppConstants.REQUEST_EXTERNAL_STORAGE)

                    }
                } else {
                    val updateIntent = Intent(activity, UpdateWithApkService::class.java)
                    updateIntent.putExtra(UpdateWithApkService.linkKey, link)
                    activity.startService(updateIntent)
                }
            }
        }.setNegativeButton(R.string.update_later, null)// TODO: 25.04.16 implement timestamp to sp
        return builder.create()
    }

}
