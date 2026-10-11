package org.stepik.android.view.certificate.ui.adapter.delegate

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import dev.androidbroadcast.vbpd.viewBinding
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.databinding.CertificateItemBinding
import org.stepic.droid.model.CertificateListItem
import org.stepik.android.model.Certificate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CertificatesAdapterDelegate(
    private val onItemClick: (String) -> Unit,
    private val onShareButtonClick: (CertificateListItem.Data) -> Unit,
    private val onChangeNameClick: (CertificateListItem.Data) -> Unit,
    private val isCurrentUser: Boolean
) : AdapterDelegate<CertificateListItem, DelegateViewHolder<CertificateListItem>>() {
    override fun isForViewType(position: Int, data: CertificateListItem): Boolean =
        data is CertificateListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CertificateListItem> =
        ViewHolder(createView(parent, R.layout.certificate_item))

    private inner class ViewHolder(
        containerView: android.view.View
    ) : DelegateViewHolder<CertificateListItem>(containerView) {
        private val viewBinding: CertificateItemBinding by viewBinding { CertificateItemBinding.bind(itemView) }

        private val certificatePlaceholder =
            ContextCompat.getDrawable(context, R.drawable.general_placeholder)

        init {
            containerView.setOnClickListener { (itemData as? CertificateListItem.Data)?.let { onItemClick(it.certificate.url ?: "") } }
            viewBinding.certificateShareButton.setOnClickListener { onShareButtonClick(itemData as CertificateListItem.Data) }
            viewBinding.certificateNameChangeButton.setOnClickListener { (itemData as? CertificateListItem.Data)?.let(onChangeNameClick) }
        }

        override fun onBind(data: CertificateListItem) {
            data as CertificateListItem.Data
            viewBinding.certificateTitle.text = data.title ?: ""

            viewBinding.certificateDescription.text =
                when (data.certificate.type) {
                    Certificate.Type.DISTINCTION ->
                        context.resources.getString(R.string.certificate_distinction_with_substitution, data.title ?: "")
                    Certificate.Type.REGULAR ->
                        context.resources.getString(R.string.certificate_regular_with_substitution, data.title ?: "")
                    else ->
                        ""
                }

            viewBinding.certificateGrade.text =
                context.resources.getString(R.string.certificate_result_with_substitution, data.certificate.grade ?: "")

            viewBinding.certificateGrade.isVisible = data.certificate.isWithScore
            viewBinding.certificateNameChangeButton.isVisible = data.certificate.editsCount < data.certificate.allowedEditsCount && isCurrentUser

            Glide.with(context)
                .load(data.coverFullPath ?: "")
                .placeholder(certificatePlaceholder)
                .into(viewBinding.certificateIcon)
        }
    }
}
