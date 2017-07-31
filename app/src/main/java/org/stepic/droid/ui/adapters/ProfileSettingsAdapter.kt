package org.stepic.droid.ui.adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.profile_item_right_arrow.view.*
import org.stepic.droid.R
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.clickProfileSettings
import org.stepic.droid.viewmodel.ProfileSettingsViewModel

class ProfileSettingsAdapter(
        private val activity: Activity,
        private val profileSettingsList: ArrayList<ProfileSettingsViewModel>,
        private val screenManager: ScreenManager
) : RecyclerView.Adapter<ProfileSettingsAdapter.Companion.SettingsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.profile_item_right_arrow, parent, false);
        return SettingsViewHolder(view, profileSettingsList, screenManager, activity)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.setData(profileSettingsList[position])
    }

    override fun getItemCount(): Int {
        return profileSettingsList.size
    }

    companion object {
        class SettingsViewHolder(itemView: View,
                                 private val profileSettingsList: ArrayList<ProfileSettingsViewModel>,
                                 private val screenManager: ScreenManager,
                                 private val activity: Activity)
            : RecyclerView.ViewHolder(itemView) {

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
                profileSettingsItem.clickProfileSettings(activity, screenManager)
            }

            fun setData(profileSettingsViewModel: ProfileSettingsViewModel) {
                itemView.optionTitle.setText(profileSettingsViewModel.stringRes)
                itemView.optionTitle.setTextColor(
                        ColorUtil.getColorArgb(profileSettingsViewModel.textColor,
                                itemView.context))
            }

        }
    }

}
