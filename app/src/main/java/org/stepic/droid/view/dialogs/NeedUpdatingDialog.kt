package org.stepic.droid.view.dialogs

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import org.stepic.droid.core.IShell
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
    lateinit var shell: IShell

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        MainApplication.component().inject(this)

        val link = arguments.getString(LINK_KEY)
        val isInGP = arguments.getBoolean(IS_IN_GP_KEY)

        val builder = AlertDialog.Builder(activity, R.style.MyAlertDialogStyle)
        builder.setTitle(R.string.update_available_title).setPositiveButton(R.string.update_now) { dialog, which ->
            YandexMetrica.reportEvent(AppConstants.UPDATING_MESSAGE_IS_APPROVED)
            if (isInGP) {
                shell.screenProvider.showStoreWithApp(activity)
            } else {

                val permissionCheck = ContextCompat.checkSelfPermission(MainApplication.getAppContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    shell.sharedPreferenceHelper.storeTempLink(link)

                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        val dialog = ExplainPermissionDialog()
                        dialog.show(activity.getFragmentManager(), null)

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
