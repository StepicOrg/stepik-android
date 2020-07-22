package org.stepik.android.view.course_list.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_course.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.doOnGlobalLayout
import org.stepik.android.domain.course.analytic.CourseCardSeenAnalyticEvent
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.view.course_list.ui.delegate.CoursePropertiesDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListItemAdapterDelegate(
    private val analytic: Analytic,
    private val onItemClicked: (CourseListItem.Data) -> Unit,
    private val onContinueCourseClicked: (CourseListItem.Data) -> Unit
) : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> =
        ViewHolder(createView(parent, R.layout.item_course))

    private inner class ViewHolder(root: View) : DelegateViewHolder<CourseListItem>(root) {
        private val courseCoverImageTarget =
            RoundedBitmapImageViewTarget(context.resources.getDimension(R.dimen.corner_radius), root.courseItemImage)

        private val coursePlaceholder = BitmapFactory
            .decodeResource(context.resources, R.drawable.general_placeholder)
            .let { bitmap ->
                RoundedBitmapDrawableFactory
                    .create(context.resources, bitmap)
                    .apply {
                        cornerRadius = context.resources.getDimension(R.dimen.corner_radius)
                    }
            }

        private val coursePropertiesDelegate = CoursePropertiesDelegate(root, root.coursePropertiesContainer as ViewGroup)
        private val courseItemName = root.courseItemName
        private val adaptiveCourseMarker = root.adaptiveCourseMarker
        private val courseContinueButton = root.courseContinueButton
        private val courseDescription = root.courseDescription
        private val courseButtonSeparator = root.courseButtonSeparator
        private val coursePrice = root.coursePrice

        init {
            root.setOnClickListener { (itemData as? CourseListItem.Data)?.let(onItemClicked) }
            courseContinueButton.setOnClickListener { (itemData as? CourseListItem.Data)?.let(onContinueCourseClicked) }
        }

        override fun onBind(data: CourseListItem) {
            data as CourseListItem.Data

            Glide
                .with(context)
                .asBitmap()
                .load(data.course.cover)
                .placeholder(coursePlaceholder)
                .fitCenter()
                .into(courseCoverImageTarget)

            courseItemName.text = data.course.title

            val isEnrolled = data.course.enrollment != 0L
            courseContinueButton.isVisible = isEnrolled
            courseButtonSeparator.isVisible = isEnrolled
            courseDescription.isVisible = !isEnrolled
            if (!isEnrolled) {
                courseDescription.text = HtmlCompat.fromHtml(data.course.summary ?: "", HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
                courseDescription.doOnGlobalLayout { it.post { it.maxLines = it.height / it.lineHeight } }
            }

            coursePrice.isVisible = !isEnrolled
            val (@ColorRes textColor, displayPrice) = if (data.course.isPaid) {
                R.color.color_overlay_violet to data.course.displayPrice
            } else {
                R.color.color_overlay_green to context.resources.getString(R.string.course_list_free)
            }
            coursePrice.setTextColor(ContextCompat.getColor(context, textColor))
            coursePrice.text = displayPrice

            adaptiveCourseMarker.isVisible = data.isAdaptive

            coursePropertiesDelegate.setStats(data)

            analytic.report(CourseCardSeenAnalyticEvent(data.course.id, data.source))
        }
    }
}