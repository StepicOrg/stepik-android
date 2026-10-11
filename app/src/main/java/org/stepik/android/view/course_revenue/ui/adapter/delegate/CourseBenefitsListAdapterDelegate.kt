package org.stepik.android.view.course_revenue.ui.adapter.delegate

import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemCourseBenefitsBinding
import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature
import org.stepik.android.view.course_revenue.mapper.RevenuePriceMapper
import org.stepik.android.view.course_revenue.model.CourseBenefitOperationItem
import ru.nobird.app.core.model.PaginationDirection
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener

class CourseBenefitsListAdapterDelegate(
    private val revenuePriceMapper: RevenuePriceMapper,
    private val onItemClick: (CourseBenefitListItem.Data) -> Unit,
    private val onFetchNextPage: () -> Unit,
    private val reloadListAction: () -> Unit
) : AdapterDelegate<CourseBenefitOperationItem, DelegateViewHolder<CourseBenefitOperationItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CourseBenefitOperationItem): Boolean =
        data is CourseBenefitOperationItem.CourseBenefits

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseBenefitOperationItem> =
        ViewHolder(createView(parent, R.layout.item_course_benefits))

    private inner class ViewHolder(
        view: android.view.View
    ) : DelegateViewHolder<CourseBenefitOperationItem>(view) {

        private val viewBinding: ItemCourseBenefitsBinding by viewBinding(ItemCourseBenefitsBinding::bind)

        private val viewStateDelegate = ViewStateDelegate<CourseBenefitsFeature.State>()
        private val adapter = DefaultDelegateAdapter<CourseBenefitListItem>()
            .also {
                it += CourseBenefitsLoadingAdapterDelegate()
                it += CourseBenefitsAdapterDelegate(revenuePriceMapper, onItemClick)
            }

        init {
            viewStateDelegate.addState<CourseBenefitsFeature.State.Loading>(viewBinding.courseBenefitsRecycler)
            viewStateDelegate.addState<CourseBenefitsFeature.State.Empty>(viewBinding.courseBenefitsEmpty.root)
            viewStateDelegate.addState<CourseBenefitsFeature.State.Error>(viewBinding.courseBenefitsError.root)
            viewStateDelegate.addState<CourseBenefitsFeature.State.Content>(viewBinding.courseBenefitsRecycler)

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
            data as CourseBenefitOperationItem.CourseBenefits
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
