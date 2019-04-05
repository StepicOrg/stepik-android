package org.stepik.android.view.profile_edit.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_profile_edit_navigation.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepik.android.view.profile_edit.model.ProfileEditItem

class ProfileEditTextDelegate(
    adapter: DelegateAdapter<ProfileEditItem, DelegateViewHolder<ProfileEditItem>>,
    private val onItemClicked: (ProfileEditItem) -> Unit
) : AdapterDelegate<ProfileEditItem, DelegateViewHolder<ProfileEditItem>>(adapter) {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<ProfileEditItem> =
        ViewHolder(createView(parent, R.layout.item_profile_edit_navigation))

    override fun isForViewType(position: Int): Boolean =
        getItemAtPosition(position) is ProfileEditItem

    inner class ViewHolder(root: View) : DelegateViewHolder<ProfileEditItem>(root) {
        private val title = root.title
        private val subtitle = root.subtitle

        init {
            root.setOnClickListener { itemData?.let(onItemClicked) }
        }

        override fun onBind(data: ProfileEditItem) {
            title.text = data.title
            subtitle.text = data.subtitle
        }
    }
}