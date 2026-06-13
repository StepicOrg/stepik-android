package org.stepik.android.view.profile_edit.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemProfileEditNavigationBinding
import org.stepik.android.view.profile_edit.model.ProfileEditItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class ProfileEditTextDelegate(
    private val onItemClicked: (ProfileEditItem) -> Unit
) : AdapterDelegate<ProfileEditItem, DelegateViewHolder<ProfileEditItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<ProfileEditItem> =
        ViewHolder(createView(parent, R.layout.item_profile_edit_navigation))

    override fun isForViewType(position: Int, data: ProfileEditItem): Boolean =
        true

    inner class ViewHolder(root: View) : DelegateViewHolder<ProfileEditItem>(root) {
        private val viewBinding: ItemProfileEditNavigationBinding by viewBinding { ItemProfileEditNavigationBinding.bind(root) }

        init {
            itemView.setOnClickListener { itemData?.let(onItemClicked) }
        }

        override fun onBind(data: ProfileEditItem) {
            viewBinding.title.text = data.title
            viewBinding.subtitle.text = data.subtitle
            itemView.isEnabled = itemData?.type != ProfileEditItem.Type.EMAIL
        }
    }
}
