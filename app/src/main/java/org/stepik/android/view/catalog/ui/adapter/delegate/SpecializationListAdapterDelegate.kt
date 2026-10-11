package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemSpecializationListBinding
import org.stepik.android.domain.catalog.model.CatalogSpecialization
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog.model.CatalogItem
import ru.nobird.app.core.model.cast
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class SpecializationListAdapterDelegate(
    private val onOpenLinkInWeb: (String) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Block && data.catalogBlockStateWrapper is CatalogBlockStateWrapper.SpecializationList

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        SpecializationListViewHolder(createView(parent, R.layout.item_specialization_list))

    private inner class SpecializationListViewHolder(
        containerView: android.view.View
    ) : DelegateViewHolder<CatalogItem>(containerView) {
        private val viewBinding: ItemSpecializationListBinding by viewBinding { ItemSpecializationListBinding.bind(itemView) }

        private val adapter = DefaultDelegateAdapter<CatalogSpecialization>()
            .also {
                it += SpecializationAdapterDelegate(onOpenLinkInWeb = onOpenLinkInWeb)
            }

        init {
            val rowCount = context.resources.getInteger(R.integer.specializations_default_rows)
            viewBinding.specializationListRecycler.layoutManager =
                TableLayoutManager(
                    context,
                    horizontalSpanCount = context.resources.getInteger(R.integer.specializations_default_columns),
                    verticalSpanCount = rowCount,
                    orientation = LinearLayoutManager.HORIZONTAL,
                    reverseLayout = false
                )
            viewBinding.specializationListRecycler.setRecycledViewPool(sharedViewPool)
            viewBinding.specializationListRecycler.setHasFixedSize(true)
            viewBinding.specializationListRecycler.adapter = adapter

            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(viewBinding.specializationListRecycler)

            viewBinding.specializationInfoAction.setOnClickListener {
                onOpenLinkInWeb(context.getString(R.string.specialization_url))
            }
        }

        override fun onBind(data: CatalogItem) {
            val specializationList = data
                .cast<CatalogItem.Block>()
                .catalogBlockStateWrapper
                .cast<CatalogBlockStateWrapper.SpecializationList>()

            adapter.items = specializationList.content.specializations
        }
    }
}