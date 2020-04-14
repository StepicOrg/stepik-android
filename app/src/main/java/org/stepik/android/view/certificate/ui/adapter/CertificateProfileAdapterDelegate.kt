package org.stepik.android.view.certificate.ui.adapter

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import org.stepic.droid.R
import org.stepic.droid.model.CertificateViewItem
import org.stepik.android.model.Certificate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CertificateProfileAdapterDelegate(
    private val onItemClick: (String) -> Unit
) : AdapterDelegate<CertificateViewItem, DelegateViewHolder<CertificateViewItem>>() {
    override fun isForViewType(position: Int, data: CertificateViewItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CertificateViewItem> =
        ViewHolder(createView(parent, R.layout.item_certificate_profile))

    private inner class ViewHolder(
        root: View
    ) : DelegateViewHolder<CertificateViewItem>(root) {

        private val certificateTextView = root as AppCompatTextView

        init {
            root.setOnClickListener { onItemClick(itemData?.certificate?.url.orEmpty()) }
        }

        override fun onBind(data: CertificateViewItem) {
            certificateTextView.text = data.title.orEmpty()

            val tintColorRes =
                when (data.certificate.type) {
                    Certificate.Type.REGULAR ->
                        R.color.certificate_regular
                    Certificate.Type.DISTINCTION ->
                        R.color.certificate_distinction
                    else ->
                        R.color.white
                }

            TextViewCompat.setCompoundDrawableTintList(certificateTextView,
                ColorStateList.valueOf(ContextCompat.getColor(context, tintColorRes)))
        }
    }
}