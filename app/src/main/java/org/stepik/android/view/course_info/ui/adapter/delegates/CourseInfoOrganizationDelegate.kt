package org.stepik.android.view.course_info.ui.adapter.delegates

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseInfoOrganizationBinding
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.model.user.User
import org.stepik.android.view.course_info.model.CourseInfoItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseInfoOrganizationDelegate(
    private val onUserClicked: ((User) -> Unit)? = null
) : AdapterDelegate<CourseInfoItem, DelegateViewHolder<CourseInfoItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder =
        ViewHolder(createView(parent, R.layout.view_course_info_organization))

    override fun isForViewType(position: Int, data: CourseInfoItem): Boolean =
        data is CourseInfoItem.OrganizationBlock

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseInfoItem>(root) {
        private val viewBinding: ViewCourseInfoOrganizationBinding by viewBinding { ViewCourseInfoOrganizationBinding.bind(root) }
        private val titleColorSpan = ForegroundColorSpan(context.resolveColorAttribute(R.attr.colorSecondary))

        init {
            root.setOnClickListener {
                (itemData as? CourseInfoItem.OrganizationBlock)
                    ?.let { data -> onUserClicked?.invoke(data.organization) }
            }
        }

        override fun onBind(data: CourseInfoItem) {
            data as CourseInfoItem.OrganizationBlock

            val title = itemView.context.getString(R.string.course_info_organization_prefix, data.organization.fullName)
            val titleSpan = SpannableString(title).apply {
                setSpan(titleColorSpan, length - (data.organization.fullName?.length ?: 0), length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
            }

            viewBinding.organizationTitle.text = titleSpan
        }
    }
}