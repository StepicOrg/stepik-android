package org.stepik.android.view.certificate.ui.adapter.delegate

import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.databinding.ItemCertificateProfileBinding
import org.stepic.droid.model.CertificateViewItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepik.android.model.Certificate
import org.stepik.android.view.step_quiz_choice.ui.delegate.LayerListDrawableDelegate

class CertificateProfileAdapterDelegate(
    private val onItemClick: (String) -> Unit
) : AdapterDelegate<CertificateViewItem, DelegateViewHolder<CertificateViewItem>>() {
    override fun isForViewType(position: Int, data: CertificateViewItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CertificateViewItem> =
        ViewHolder(createView(parent, R.layout.item_certificate_profile))

    private inner class ViewHolder(root: View) : DelegateViewHolder<CertificateViewItem>(root) {
        private val viewBinding: ItemCertificateProfileBinding by viewBinding { ItemCertificateProfileBinding.bind(root) }
        private val layerListDrawableDelegate = LayerListDrawableDelegate(
            listOf(
                R.id.certificate_regular,
                R.id.certificate_distinction
            ),
            (viewBinding.root.background as RippleDrawable).findDrawableByLayerId(R.id.certificate_layer_list) as LayerDrawable
        )

        init {
            viewBinding.root.setOnClickListener { onItemClick(itemData?.certificate?.url.orEmpty()) }
        }

        override fun onBind(data: CertificateViewItem) {
            viewBinding.certificateCourseTitle.text = data.title.orEmpty()

            val certificateColor =
                when (data.certificate.type) {
                    Certificate.Type.REGULAR ->
                        ContextCompat.getColor(context, R.color.certificate_regular)
                    Certificate.Type.DISTINCTION ->
                        ContextCompat.getColor(context, R.color.certificate_distinction)
                    else ->
                        ContextCompat.getColor(context, R.color.white)
                }

            val hasGrade = data.certificate.grade != null

            viewBinding.certificateProgress.isVisible = hasGrade
            viewBinding.certificateProgressPercentage.isVisible = hasGrade
            viewBinding.certificateCourseTitle.maxLines =
                if (hasGrade) {
                    2
                } else {
                    3
                }

            layerListDrawableDelegate.showLayer(getRootBackgroundLayer(data.certificate))
            viewBinding.certificateProgress.setIndicatorColor(certificateColor)
            viewBinding.certificateProgress.max = 100
            viewBinding.certificateProgress.progress = data.certificate.grade?.toIntOrNull() ?: 0
            viewBinding.certificateProgressPercentage.setTextColor(certificateColor)
            viewBinding.certificateProgressPercentage.text = context.getString(R.string.certificates_percentage, data.certificate.grade)

            Glide.with(context)
                .load(data.coverFullPath)
                .placeholder(ContextCompat.getDrawable(context, R.drawable.general_placeholder))
                .into(viewBinding.certificateCourseIcon)
        }

        private fun getRootBackgroundLayer(certificate: Certificate): Int =
            when (certificate.type) {
                Certificate.Type.REGULAR ->
                    R.id.certificate_regular
                Certificate.Type.DISTINCTION ->
                    R.id.certificate_distinction
                else ->
                    throw IllegalArgumentException("Invalid certificate type")
            }
    }
}