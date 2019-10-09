package org.stepic.droid.ui.adapters

import android.app.Activity
import androidx.core.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.clickProfileSettings
import org.stepic.droid.viewmodel.ProfileSettingsViewModel

class ProfileSettingsAdapter(
        private val activity: Activity,
        private val profileSettingsList: ArrayList<ProfileSettingsViewModel>,
        private val screenManager: ScreenManager,
        private val fragment: Fragment,
        private val analytic: Analytic
) : RecyclerView.Adapter<ProfileSettingsAdapter.SettingsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.profile_item_right_arrow, parent, false)
        return SettingsViewHolder(view, profileSettingsList, screenManager, activity, fragment, analytic)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.setData(profileSettingsList[position])
    }

    override fun getItemCount(): Int = profileSettingsList.size

    class SettingsViewHolder(itemView: View,
                             private val profileSettingsList: ArrayList<ProfileSettingsViewModel>,
                             private val screenManager: ScreenManager,
                             private val activity: Activity,
                             private val fragment: Fragment,
                             private val analytic: Analytic)
        : RecyclerView.ViewHolder(itemView) {

        private val optionTitle = itemView.findViewById<TextView>(R.id.optionTitle)

        init {
            itemView.setOnClickListener {
                clickOnPosition(adapterPosition)
            }
        }

        private fun clickOnPosition(adapterPosition: Int) {
            if (adapterPosition < 0 || adapterPosition >= profileSettingsList.size) {
                return
            }

            val profileSettingsItem = profileSettingsList[adapterPosition]

            profileSettingsItem.clickProfileSettings(activity, screenManager, fragment, analytic)
        }

        fun setData(profileSettingsViewModel: ProfileSettingsViewModel) {
            optionTitle.setText(profileSettingsViewModel.stringRes)
            optionTitle.setTextColor(
                    ColorUtil.getColorArgb(profileSettingsViewModel.textColor, itemView.context))
        }

    }

}
