package org.stepik.android.view.catalog.ui.delegate

import android.view.View
import androidx.core.view.isVisible
import org.stepic.droid.databinding.HeaderCatalogBlockBinding
import org.stepik.android.domain.catalog.model.CatalogBlock

class CatalogBlockHeaderDelegate(
    private val rootView: View,
    onClickListener: View.OnClickListener? = null
) {
    private val binding: HeaderCatalogBlockBinding = HeaderCatalogBlockBinding.bind(rootView)

    init {
        onClickListener?.let { rootView.setOnClickListener(it) }
        binding.containerViewAll.isVisible = onClickListener != null
    }

    fun setInformation(data: CatalogBlock) {
        rootView.isVisible = data.isTitleVisible
        binding.containerTitle.text = data.title

        binding.containerDescription.text = data.description
        binding.containerDescription.isVisible = data.description.isNotEmpty()
    }

    fun setCount(countText: String) {
        binding.containerCarouselCount.isVisible = true
        binding.containerCarouselCount.text = countText
    }
}
