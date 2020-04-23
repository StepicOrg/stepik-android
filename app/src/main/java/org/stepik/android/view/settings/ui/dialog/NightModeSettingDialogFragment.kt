package org.stepik.android.view.settings.ui.dialog

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class NightModeSettingDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "NightModeSettingDialogFragment"

        fun newInstance(): DialogFragment =
            NightModeSettingDialogFragment()
    }

    @Inject
    internal lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private val nightModeValues =
        if (Build.VERSION.SDK_INT >= 29) {
            arrayOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            arrayOf(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    private fun injectComponent() {
        App.component()
            .settingsComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.setting_night_mode)
            .setSingleChoiceItems(resources.getStringArray(R.array.night_modes), nightModeValues.indexOf(sharedPreferenceHelper.nightMode)) { _, which ->
                sharedPreferenceHelper.nightMode = nightModeValues[which]
                AppCompatDelegate.setDefaultNightMode(nightModeValues[which])
                dismiss()
            }
            .create()
}