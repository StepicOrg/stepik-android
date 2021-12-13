package org.stepik.android.view.debug.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.android.billingclient.api.Purchase
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.debug.item_in_app_purchase.*
import org.stepic.droid.R
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

    private inner class ViewHolder(override val containerView: View) : DelegateViewHolder<Purchase>(containerView), LayoutContainer {
        init {
            inAppPurchaseConsumeAction.setOnClickListener { itemData?.let(onItemClick) }
        }
        override fun onBind(data: Purchase) {
            inAppPurchaseSku.text = data.skus.first()
            inAppPurchaseTime.text = context.getString(R.string.debug_purchase_date, DateTimeHelper.getPrintableDate(Date(data.purchaseTime), DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault()))
            inAppPurchaseStatus.text = context.getString(R.string.debug_purchase_status, data.purchaseState.toString())

            inAppPurchaseCourse.isVisible = data.developerPayload.isNotEmpty()
            inAppPurchaseUser.isVisible = data.developerPayload.isNotEmpty()
            if (data.developerPayload.isNotEmpty()) {
                data.developerPayload.toObject<CoursePurchasePayload>().let {
                    inAppPurchaseCourse.text = context.getString(R.string.debug_purchase_course, it.courseId)
                    inAppPurchaseUser.text = context.getString(R.string.debug_purchase_profile, it.profileId)
                }
            }
        }
    }
}