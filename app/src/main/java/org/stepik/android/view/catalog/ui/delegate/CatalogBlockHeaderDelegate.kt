package org.stepik.android.view.catalog.ui.delegate

import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.header_catalog_block.*
import org.stepik.android.domain.catalog.model.CatalogBlock

class CatalogBlockHeaderDelegate(
    override val containerView: View,
    onClickListener: View.OnClickListener? = null
) : LayoutContainer {
    init {
        onClickListener?.let { containerView.setOnClickListener(it) }
        containerViewAll.isVisible = onClickListener != null
    }

    fun setInformation(data: CatalogBlock) {
        containerView.isVisible = data.isTitleVisible
        containerTitle.text = data.title

        containerDescription.text = data.description
        containerDescription.isVisible = data.description.isNotEmpty()
    }

    fun setCount(countText: String) {
        containerCarouselCount.isVisible = true
        containerCarouselCount.text = countText
    }
}