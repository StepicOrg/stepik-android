package org.stepik.android.view.catalog_block.ui.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_container_block.view.*
import org.stepik.android.domain.catalog_block.model.CatalogBlock

class CatalogBlockTitleDelegate(
    private val view: ViewGroup,
    onClickListener: View.OnClickListener? = null
) {
    private val root = view.catalogBlockContainer
    private val title = view.containerTitle
    private val count = view.containerCarouselCount
    private val viewAllArrow = view.containerViewAll
    private val description = view.containerDescription

    init {
        onClickListener?.let { root.setOnClickListener(it) }
        viewAllArrow.isVisible = onClickListener != null
    }

    fun setInformation(data: CatalogBlock) {
        view.isVisible = data.isTitleVisible
        title.text = data.title

        description.text = data.description
        description.isVisible = data.description.isNotEmpty()
    }

    fun setCount(countText: String) {
        count.isVisible = true
        count.text = countText
    }
}