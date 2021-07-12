package org.stepik.android.view.course_revenue.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_course_benefits.*
import org.stepic.droid.R
import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course_revenue.model.CourseBenefitOperationItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate

class CourseBenefitsListAdapterDelegate(
    private val displayPriceMapper: DisplayPriceMapper,
    private val onItemClick: (CourseBenefitListItem.Data) -> Unit
) : AdapterDelegate<CourseBenefitOperationItem, DelegateViewHolder<CourseBenefitOperationItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CourseBenefitOperationItem): Boolean =
        data is CourseBenefitOperationItem.PurchasesAndRefunds

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitOperationItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefits))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseBenefitOperationItem>(containerView), LayoutContainer {

        private val viewStateDelegate = ViewStateDelegate<CourseBenefitsFeature.State>()
        private val adapter = DefaultDelegateAdapter<CourseBenefitListItem>()
            .also {
                it += CourseBenefitsListLoadingAdapterDelegate()
                it += CourseBenefitsAdapterDelegate(displayPriceMapper, onItemClick)
            }

        init {
            viewStateDelegate.addState<CourseBenefitsFeature.State.Loading>(purchaseRefundRecycler)
            viewStateDelegate.addState<CourseBenefitsFeature.State.Empty>(purchaseEmpty)
            viewStateDelegate.addState<CourseBenefitsFeature.State.Error>(purchaseRefundError)
            viewStateDelegate.addState<CourseBenefitsFeature.State.Content>(purchaseRefundRecycler)

            purchaseRefundRecycler.adapter = adapter
            purchaseRefundRecycler.layoutManager = LinearLayoutManager(context)
            purchaseRefundRecycler.setRecycledViewPool(sharedViewPool)
            purchaseRefundRecycler.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)?.let(::setDrawable)
            })
        }

        override fun onBind(data: CourseBenefitOperationItem) {
            data as CourseBenefitOperationItem.PurchasesAndRefunds
            render(data.state)
        }

        private fun render(state: CourseBenefitsFeature.State) {
            viewStateDelegate.switchState(state)
            if (state is CourseBenefitsFeature.State.Loading) {
                adapter.items = listOf(
                    CourseBenefitListItem.Placeholder,
                    CourseBenefitListItem.Placeholder,
                    CourseBenefitListItem.Placeholder,
                    CourseBenefitListItem.Placeholder,
                    CourseBenefitListItem.Placeholder
                )
            }
            if (state is CourseBenefitsFeature.State.Content) {
                adapter.items = state.courseBenefitListItems
            }
        }
    }
}