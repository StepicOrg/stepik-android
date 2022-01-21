package org.stepik.android.view.certificate.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.certificate_item.view.*
import org.stepic.droid.R
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
        root: View
    ) : DelegateViewHolder<CertificateListItem>(root) {

        private val certificateTitleView = root.certificate_title
        private val certificateIcon = root.certificate_icon
        private val certificateGradeView = root.certificate_grade
        private val certificateDescription = root.certificate_description
        private val certificateShareButton = root.certificate_share_button
        private val certificateNameChangeButton = root.certificate_name_change_button

        private val certificatePlaceholder =
            ContextCompat.getDrawable(context, R.drawable.general_placeholder)

        init {
            root.setOnClickListener { (itemData as? CertificateListItem.Data)?.let { onItemClick(it.certificate.url ?: "") } }
            certificateShareButton.setOnClickListener { onShareButtonClick(itemData as CertificateListItem.Data) }
            certificateNameChangeButton.setOnClickListener { (itemData as? CertificateListItem.Data)?.let(onChangeNameClick) }
        }

        override fun onBind(data: CertificateListItem) {
            data as CertificateListItem.Data
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

            certificateGradeView.isVisible = data.certificate.isWithScore
            certificateNameChangeButton.isVisible = data.certificate.editsCount < data.certificate.allowedEditsCount && isCurrentUser

            Glide.with(context)
                .load(data.coverFullPath ?: "")
                .placeholder(certificatePlaceholder)
                .into(certificateIcon)
        }
    }
}