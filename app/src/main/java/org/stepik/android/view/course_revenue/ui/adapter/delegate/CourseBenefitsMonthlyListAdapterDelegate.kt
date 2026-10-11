package org.stepik.android.view.course_revenue.ui.adapter.delegate

import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemCourseBenefitsBinding
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature
import org.stepik.android.view.course_revenue.mapper.RevenuePriceMapper
import org.stepik.android.view.course_revenue.model.CourseBenefitOperationItem
import ru.nobird.app.core.model.PaginationDirection
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener

class CourseBenefitsMonthlyListAdapterDelegate(
    private val revenuePriceMapper: RevenuePriceMapper,
    private val onFetchNextPage: () -> Unit,
    private val reloadListAction: () -> Unit
) : AdapterDelegate<CourseBenefitOperationItem, DelegateViewHolder<CourseBenefitOperationItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CourseBenefitOperationItem): Boolean =
        data is CourseBenefitOperationItem.CourseBenefitsMonthly

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitOperationItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefits))

    private inner class ViewHolder(
        view: android.view.View
    ) : DelegateViewHolder<CourseBenefitOperationItem>(view) {

        private val viewBinding: ItemCourseBenefitsBinding by viewBinding(ItemCourseBenefitsBinding::bind)

        private val viewStateDelegate = ViewStateDelegate<CourseBenefitsMonthlyFeature.State>()
        private val adapter = DefaultDelegateAdapter<CourseBenefitByMonthListItem>()
            .also {
                it += CourseBenefitsMonthlyLoadingAdapterDelegate()
                it += CourseBenefitsMonthlyAdapterDelegate(revenuePriceMapper)
            }

        init {
            viewStateDelegate.addState<CourseBenefitsMonthlyFeature.State.Loading>(viewBinding.courseBenefitsRecycler)
            viewStateDelegate.addState<CourseBenefitsMonthlyFeature.State.Empty>(viewBinding.courseBenefitsEmpty.root)
            viewStateDelegate.addState<CourseBenefitsMonthlyFeature.State.Error>(viewBinding.courseBenefitsError.root)
            viewStateDelegate.addState<CourseBenefitsMonthlyFeature.State.Content>(viewBinding.courseBenefitsRecycler)

            viewBinding.courseBenefitsRecycler.adapter = adapter
            viewBinding.courseBenefitsRecycler.layoutManager = LinearLayoutManager(context)
            viewBinding.courseBenefitsRecycler.setRecycledViewPool(sharedViewPool)
            viewBinding.courseBenefitsRecycler.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)?.let(::setDrawable)
            })
            viewBinding.courseBenefitsRecycler.setOnPaginationListener { direction ->
                if (direction == PaginationDirection.NEXT) {
                    onFetchNextPage()
                }
            }

            viewBinding.courseBenefitsError.tryAgain.setOnClickListener { reloadListAction() }
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
