package org.stepik.android.view.debug.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import com.android.billingclient.api.Purchase
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemInAppPurchaseBinding
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.toObject
import org.stepik.android.domain.course.model.CoursePurchasePayload
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.Date
import java.util.TimeZone

class InAppPurchaseAdapterDelegate(
    private val onItemClick: (Purchase) -> Unit
) : AdapterDelegate<Purchase, DelegateViewHolder<Purchase>>() {
    override fun isForViewType(position: Int, data: Purchase): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<Purchase> =
        ViewHolder(createView(parent, R.layout.item_in_app_purchase))

    private inner class ViewHolder(containerView: View) : DelegateViewHolder<Purchase>(containerView) {
        private val viewBinding: ItemInAppPurchaseBinding by viewBinding { ItemInAppPurchaseBinding.bind(itemView) }

        init {
            viewBinding.inAppPurchaseConsumeAction.setOnClickListener { itemData?.let(onItemClick) }
        }
        override fun onBind(data: Purchase) {
            viewBinding.inAppPurchaseSku.text = data.skus.first()
            viewBinding.inAppPurchaseTime.text = context.getString(R.string.debug_purchase_date, DateTimeHelper.getPrintableDate(Date(data.purchaseTime), DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault()))
            viewBinding.inAppPurchaseStatus.text = context.getString(R.string.debug_purchase_status, data.purchaseState.toString())

            viewBinding.inAppPurchaseCourse.isVisible = data.developerPayload.isNotEmpty()
            viewBinding.inAppPurchaseUser.isVisible = data.developerPayload.isNotEmpty()
            if (data.developerPayload.isNotEmpty()) {
                data.developerPayload.toObject<CoursePurchasePayload>().let {
                    viewBinding.inAppPurchaseCourse.text = context.getString(R.string.debug_purchase_course, it.courseId)
                    viewBinding.inAppPurchaseUser.text = context.getString(R.string.debug_purchase_profile, it.profileId)
                }
            }
        }
    }
}
