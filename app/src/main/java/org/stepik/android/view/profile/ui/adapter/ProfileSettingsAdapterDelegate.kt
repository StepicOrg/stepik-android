package org.stepik.android.view.profile.ui.adapter

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.profile_item_right_arrow.view.*
import org.stepic.droid.R
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.viewmodel.ProfileSettingsViewModel
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class ProfileSettingsAdapterDelegate(
    private val onItemClick: (optionStringId: Int) -> Unit
) : AdapterDelegate<ProfileSettingsViewModel, DelegateViewHolder<ProfileSettingsViewModel>>() {
    override fun isForViewType(position: Int, data: ProfileSettingsViewModel): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<ProfileSettingsViewModel> =
        ViewHolder(createView(parent, R.layout.profile_item_right_arrow))

    private inner class ViewHolder(root: View) : DelegateViewHolder<ProfileSettingsViewModel>(root) {

        private val optionTitle = itemView.optionTitle

        init {
            root.setOnClickListener { onItemClick(itemData?.stringRes ?: 0) }
        }

        override fun onBind(data: ProfileSettingsViewModel) {
            optionTitle.text = context.getString(data.stringRes)
            optionTitle.setTextColor(ColorUtil.getColorArgb(data.textColor, context))
        }
    }
}