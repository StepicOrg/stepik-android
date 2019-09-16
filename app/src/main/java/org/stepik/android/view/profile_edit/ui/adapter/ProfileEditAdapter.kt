package org.stepik.android.view.profile_edit.ui.adapter

import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepik.android.view.profile_edit.model.ProfileEditItem
import org.stepik.android.view.profile_edit.ui.adapter.delegates.ProfileEditTextDelegate

class ProfileEditAdapter(
    private val items: List<ProfileEditItem>,
    onItemClicked: (ProfileEditItem) -> Unit
) : DelegateAdapter<ProfileEditItem, DelegateViewHolder<ProfileEditItem>>() {
    init {
        addDelegate(ProfileEditTextDelegate(onItemClicked))
    }

    override fun getItemAtPosition(position: Int): ProfileEditItem =
        items[position]

    override fun getItemCount(): Int =
        items.size

    override fun onBindViewHolder(holder: DelegateViewHolder<ProfileEditItem>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (items[position].type == ProfileEditItem.Type.EMAIL) {
            holder.itemView.isEnabled = false
        }
    }
}