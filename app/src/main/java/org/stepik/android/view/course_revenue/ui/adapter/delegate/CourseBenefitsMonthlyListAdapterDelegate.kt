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
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course_revenue.model.CourseBenefitOperationItem
import ru.nobird.android.core.model.PaginationDirection
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener

class CourseBenefitsMonthlyListAdapterDelegate(
    private val displayPriceMapper: DisplayPriceMapper,
    private val onFetchNextPage: () -> Unit
) : AdapterDelegate<CourseBenefitOperationItem, DelegateViewHolder<CourseBenefitOperationItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CourseBenefitOperationItem): Boolean =
        data is CourseBenefitOperationItem.CourseBenefitsMonthly

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitOperationItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefits))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseBenefitOperationItem>(containerView), LayoutContainer {

        private val viewStateDelegate = ViewStateDelegate<CourseBenefitsMonthlyFeature.State>()
        private val adapter = DefaultDelegateAdapter<CourseBenefitByMonthListItem>()
            .also {
                it += CourseBenefitsMonthlyLoadingAdapterDelegate()
                it += CourseBenefitsMonthlyAdapterDelegate(displayPriceMapper)
            }

        init {
            viewStateDelegate.addState<CourseBenefitsMonthlyFeature.State.Loading>(courseBenefitsRecycler)
            viewStateDelegate.addState<CourseBenefitsMonthlyFeature.State.Empty>(courseBenefitsEmpty)
            viewStateDelegate.addState<CourseBenefitsMonthlyFeature.State.Error>(courseBenefitsError)
            viewStateDelegate.addState<CourseBenefitsMonthlyFeature.State.Content>(courseBenefitsRecycler)

            courseBenefitsRecycler.adapter = adapter
            courseBenefitsRecycler.layoutManager = LinearLayoutManager(context)
            courseBenefitsRecycler.setRecycledViewPool(sharedViewPool)
            courseBenefitsRecycler.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)?.let(::setDrawable)
            })
            courseBenefitsRecycler.setOnPaginationListener { direction ->
                if (direction == PaginationDirection.NEXT) {
                    val state = ((itemData as? CourseBenefitOperationItem.CourseBenefitsMonthly)
                        ?.state as? CourseBenefitsMonthlyFeature.State.Content)
                        ?: return@setOnPaginationListener

                    if (state.courseBenefitByMonthListDataItems.hasNext) {
                        onFetchNextPage()
                    }
                }
            }
        }

        override fun onBind(data: CourseBenefitOperationItem) {
            data as CourseBenefitOperationItem.CourseBenefitsMonthly
            render(data.state)
        }

        private fun render(state: CourseBenefitsMonthlyFeature.State) {
            viewStateDelegate.switchState(state)
            if (state is CourseBenefitsMonthlyFeature.State.Loading) {
                adapter.items = listOf(CourseBenefitByMonthListItem.Placeholder, CourseBenefitByMonthListItem.Placeholder)
            }
            if (state is CourseBenefitsMonthlyFeature.State.Content) {
                adapter.items = state.courseBenefitByMonthListItems
            }
        }
    }
}