package org.stepic.droid.features.course.ui.adapter.course_info.delegates

import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_info_organization.view.*
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.adapter.course_info.CourseInfoAdapter
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate

class CourseInfoOrganizationDelegate(
        adapter: CourseInfoAdapter
) : AdapterDelegate<CourseInfoItem, CourseInfoAdapter.ViewHolder>(adapter) {
    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_info_organization))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) is CourseInfoItem.OrganizationBlock

    class ViewHolder(root: View) : CourseInfoAdapter.ViewHolder(root) {
        private val titleColorSpan = ForegroundColorSpan(ContextCompat.getColor(root.context, R.color.course_info_organization_span))
        private val organizationTitle = root.organizationTitle

        override fun onBind(data: CourseInfoItem) {
            data as CourseInfoItem.OrganizationBlock

            val title = itemView.context.getString(R.string.course_info_organization_prefix, data.organizationTitle)
            val titleSpan = SpannableString(title).apply {
                setSpan(titleColorSpan, length - data.organizationTitle.length, length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
            }

            organizationTitle.text = titleSpan
        }
    }
}