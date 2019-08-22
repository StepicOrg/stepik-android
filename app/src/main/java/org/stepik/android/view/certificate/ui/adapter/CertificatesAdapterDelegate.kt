package org.stepik.android.view.certificate.ui.adapter

import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.certificate_item.view.*
import org.stepic.droid.R
import org.stepic.droid.model.CertificateViewItem
import org.stepik.android.model.Certificate
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class CertificatesAdapterDelegate(
    private val onItemClick: (String) -> Unit,
    private val onShareButtonClick: (CertificateViewItem) -> Unit
) : AdapterDelegate<CertificateViewItem, DelegateViewHolder<CertificateViewItem>>() {
    override fun isForViewType(position: Int, data: CertificateViewItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CertificateViewItem> =
        ViewHolder(createView(parent, R.layout.certificate_item))

    private inner class ViewHolder(
        root: View
    ) : DelegateViewHolder<CertificateViewItem>(root) {

        private val certificateTitleView = root.certificate_title
        private val certificateIcon = root.certificate_icon
        private val certificateGradeView = root.certificate_grade
        private val certificateDescription = root.certificate_description
        private val certificateShareButton = root.certificate_share_button

        private val certificatePlaceholder =
            ContextCompat.getDrawable(context, R.drawable.general_placeholder)

        init {
            root.setOnClickListener { onItemClick(itemData?.certificate?.url ?: "") }
            certificateShareButton.setOnClickListener { onShareButtonClick(itemData as CertificateViewItem) }
        }

        override fun onBind(data: CertificateViewItem) {
            certificateTitleView.text = data.title ?: ""

            certificateDescription.text =
                when (data.certificate.type) {
                    Certificate.Type.DISTINCTION ->
                        context.resources.getString(R.string.certificate_distinction_with_substitution, data.title ?: "")
                    Certificate.Type.REGULAR ->
                        context.resources.getString(R.string.certificate_regular_with_substitution, data.title ?: "")
                    else ->
                        ""
                }

            certificateGradeView.text =
                context.resources.getString(R.string.certificate_result_with_substitution, data.certificate.grade ?: "")

            Glide.with(context)
                .load(data.coverFullPath ?: "")
                .placeholder(certificatePlaceholder)
                .into(certificateIcon)
        }
    }
}