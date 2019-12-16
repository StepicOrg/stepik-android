package org.stepik.android.view.certificate.ui.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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
        ViewHolder(createView(parent, R.layout.certificate_profile_item))

    private inner class ViewHolder(
        root: View
    ) : DelegateViewHolder<CertificateViewItem>(root) {

        private val certificateTextView = root as AppCompatTextView

        init {
            root.setOnClickListener { onItemClick(itemData?.certificate?.url ?: "") }
        }

        override fun onBind(data: CertificateViewItem) {
            certificateTextView.text = data.title ?: ""

            val draw = AppCompatResources
                .getDrawable(context, R.drawable.ic_certificate)
                ?.let(DrawableCompat::wrap)
                ?.let(Drawable::mutate)

            if (draw != null) {
                val colorRes = when (data.certificate.type) {
                    Certificate.Type.REGULAR ->
                        R.color.certificate_regular
                    Certificate.Type.DISTINCTION ->
                        R.color.certificate_distinction
                    else ->
                        R.color.white
                }
                DrawableCompat.setTint(draw, ContextCompat.getColor(context, colorRes))
                certificateTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(draw, null, null, null)
            }
        }
    }
}