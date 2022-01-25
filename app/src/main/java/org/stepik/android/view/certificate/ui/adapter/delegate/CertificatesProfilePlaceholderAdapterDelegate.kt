package org.stepik.android.view.certificate.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.model.CertificateListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CertificatesProfilePlaceholderAdapterDelegate : AdapterDelegate<CertificateListItem, DelegateViewHolder<CertificateListItem>>() {
    override fun isForViewType(position: Int, data: CertificateListItem): Boolean =
        data is CertificateListItem.Placeholder

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CertificateListItem> =
        ViewHolder(createView(parent, R.layout.item_certificate_profile_skeleton))

    private class ViewHolder(root: View) : DelegateViewHolder<CertificateListItem>(root)
}