package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.strikeThrough
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemSpecializationBinding
import org.stepik.android.domain.catalog.model.CatalogSpecialization
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SpecializationAdapterDelegate(
    private val onOpenLinkInWeb: (String) -> Unit
) : AdapterDelegate<CatalogSpecialization, DelegateViewHolder<CatalogSpecialization>>() {
    companion object {
        private const val RUB_FORMAT = "RUB"
        private const val USD_FORMAT = "USD"

        private const val PRICE_SUFFIX = ".00"
    }

    override fun isForViewType(position: Int, data: CatalogSpecialization): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogSpecialization> =
        ViewHolder(createView(parent, R.layout.item_specialization))

    private inner class ViewHolder(
        containerView: android.view.View
    ) : DelegateViewHolder<CatalogSpecialization>(containerView) {
        private val viewBinding: ItemSpecializationBinding by viewBinding { ItemSpecializationBinding.bind(itemView) }

        init {
            containerView.setOnClickListener {
                itemData?.let {
                    onOpenLinkInWeb(it.detailsUrl)
                }
            }
        }

        override fun onBind(data: CatalogSpecialization) {
            viewBinding.specializationTitle.text = data.title
            viewBinding.specializationDuration.text = data.duration
            val discount = data.discount?.toFloatOrNull() ?: 0f

            if (discount > 0f && data.discount != null) {
                viewBinding.specializationPrice.text = formatDisplayPrice(data.discount.removeSuffix(PRICE_SUFFIX), data.currency)
                viewBinding.specializationDiscountPrice.text = buildSpannedString {
                    strikeThrough {
                        append(formatDisplayPrice(data.price.removeSuffix(PRICE_SUFFIX), data.currency))
                    }
                }
            } else {
                viewBinding.specializationPrice.text = formatDisplayPrice(data.price.removeSuffix(PRICE_SUFFIX), data.currency)
            }
        }

        private fun formatDisplayPrice(price: String, currencyCode: String): String =
            when (currencyCode) {
                RUB_FORMAT ->
                    context.getString(R.string.rub_format, price)
                USD_FORMAT ->
                    context.getString(R.string.usd_format, price)
                else ->
                    "$price $currencyCode"
            }
    }
}