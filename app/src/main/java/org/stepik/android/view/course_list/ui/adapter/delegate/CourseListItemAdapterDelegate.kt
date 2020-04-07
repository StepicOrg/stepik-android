package org.stepik.android.view.course_list.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.new_course_item.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.doOnGlobalLayout
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.view.course_list.ui.delegate.CoursePropertiesDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListItemAdapterDelegate(
    private val onItemClicked: (CourseListItem.Data) -> Unit,
    private val onContinueCourseClicked: (CourseListItem.Data) -> Unit
) : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> =
        ViewHolder(createView(parent, R.layout.new_course_item))

    private inner class ViewHolder(root: View) : DelegateViewHolder<CourseListItem>(root) {
        private val courseCoverImageTarget =
            RoundedBitmapImageViewTarget(context.resources.getDimension(R.dimen.course_image_radius), root.courseItemImage)

        private val coursePlaceholder = BitmapFactory
            .decodeResource(context.resources, R.drawable.general_placeholder)
            .let { bitmap ->
                RoundedBitmapDrawableFactory
                    .create(context.resources, bitmap)
                    .apply {
                        cornerRadius = context.resources.getDimension(R.dimen.course_image_radius)
                    }
            }

        private val coursePropertiesDelegate = CoursePropertiesDelegate(root.coursePropertiesContainer as ViewGroup)
        private val courseItemName = root.courseItemName
        private val adaptiveCourseMarker = root.adaptiveCourseMarker
        private val courseContinueButton = root.courseContinueButton
        private val courseDescription = root.courseDescription
        private val courseButtonSeparator = root.courseButtonSeparator

        init {
            coursePropertiesDelegate.setTextColor(ContextCompat.getColor(context, R.color.new_accent_color))

            root.setOnClickListener {
                val dataItem = itemData as? CourseListItem.Data
                    ?: return@setOnClickListener

                dataItem.let(onItemClicked)
            }

            courseContinueButton.setOnClickListener {
                val dataItem = itemData as? CourseListItem.Data
                    ?: return@setOnClickListener

                dataItem.let(onContinueCourseClicked)
            }
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

            adaptiveCourseMarker.isVisible = data.isAdaptive

            // TODO Handle in delegate
            coursePropertiesDelegate.setStats(data.course)
        }
    }
}