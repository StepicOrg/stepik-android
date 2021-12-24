package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.takusemba.multisnaprecyclerview.MultiSnapHelper
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_specialization_list.*
import org.stepic.droid.R
import org.stepik.android.domain.catalog.model.CatalogSpecialization
import org.stepik.android.presentation.course_list_redux.model.CatalogBlockStateWrapper
import org.stepik.android.view.base.ui.adapter.layoutmanager.TableLayoutManager
import org.stepik.android.view.catalog.model.CatalogItem
import ru.nobird.android.core.model.cast
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
        override val containerView: View
    ) : DelegateViewHolder<CatalogItem>(containerView), LayoutContainer {

        private val adapter = DefaultDelegateAdapter<CatalogSpecialization>()
            .also {
                it += SpecializationAdapterDelegate(onOpenLinkInWeb = onOpenLinkInWeb)
            }

        init {
            val rowCount = context.resources.getInteger(R.integer.specializations_default_rows)
            specializationListRecycler.layoutManager =
                TableLayoutManager(
                    context,
                    horizontalSpanCount = context.resources.getInteger(R.integer.specializations_default_columns),
                    verticalSpanCount = rowCount,
                    orientation = LinearLayoutManager.HORIZONTAL,
                    reverseLayout = false
                )
            specializationListRecycler.setRecycledViewPool(sharedViewPool)
            specializationListRecycler.setHasFixedSize(true)
            specializationListRecycler.adapter = adapter

            val snapHelper = MultiSnapHelper(interval = 1)
            snapHelper.attachToRecyclerView(specializationListRecycler)

            specializationInfoAction.setOnClickListener {
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