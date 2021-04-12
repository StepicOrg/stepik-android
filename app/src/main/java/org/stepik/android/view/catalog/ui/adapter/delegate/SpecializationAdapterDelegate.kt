package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.strikeThrough
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_specialization.*
import org.stepic.droid.R
import org.stepik.android.domain.catalog.model.CatalogSpecialization
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SpecializationAdapterDelegate(
    private val onOpenLinkInWeb: (String, String) -> Unit
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
        override val containerView: View
    ) : DelegateViewHolder<CatalogSpecialization>(containerView), LayoutContainer {

        init {
            containerView.setOnClickListener {
                itemData?.let {
                    onOpenLinkInWeb(it.title, it.detailsUrl)
                }
            }
        }

        override fun onBind(data: CatalogSpecialization) {
            specializationTitle.text = data.title
            specializationDuration.text = data.duration
            val discount = data.discount?.toFloatOrNull() ?: 0f

            // TODO APPS-3190 Find out about colors in dark theme
            if (discount > 0f && data.discount != null) {
                specializationPrice.text = formatDisplayPrice(data.discount.removeSuffix(PRICE_SUFFIX), data.currency)
                specializationDiscountPrice.text = buildSpannedString {
                    strikeThrough {
                        append(formatDisplayPrice(data.price.removeSuffix(PRICE_SUFFIX), data.currency))
                    }
                }
            } else {
                specializationPrice.text = formatDisplayPrice(data.price.removeSuffix(PRICE_SUFFIX), data.currency)
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