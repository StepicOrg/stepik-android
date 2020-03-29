package org.stepik.android.view.course_list.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.new_course_item.view.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.doOnGlobalLayout
import org.stepik.android.model.Course
import org.stepik.android.view.course_list.ui.delegate.CoursePropertiesDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseAdapterDelegate(
    private val adaptiveCoursesResolver: AdaptiveCoursesResolver,
    private val onItemClicked: (Course) -> Unit,
    private val onContinueCourseClicked: (Course) -> Unit
) : AdapterDelegate<Course, DelegateViewHolder<Course>>() {
    override fun isForViewType(position: Int, data: Course): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<Course> =
        ViewHolder(createView(parent, R.layout.new_course_item))

    private inner class ViewHolder(root: View) : DelegateViewHolder<Course>(root) {
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

        private val coursePropertiesDelegate = CoursePropertiesDelegate(root.coursePropertiesContainer as ViewGroup)
        private val courseItemName = root.courseItemName
        private val adaptiveCourseMarker = root.adaptiveCourseMarker
        private val courseContinueButton = root.courseContinueButton
        private val courseDescription = root.courseDescription
        private val courseButtonSeparator = root.courseButtonSeparator

        init {
            root.setOnClickListener { itemData?.let(onItemClicked) }
            courseContinueButton.setOnClickListener { itemData?.let(onContinueCourseClicked) }
        }

        override fun onBind(data: Course) {
            Glide
                .with(context)
                .asBitmap()
                .load(data.cover)
                .placeholder(coursePlaceholder)
                .fitCenter()
                .into(courseCoverImageTarget)

            courseItemName.text = data.title

            val isEnrolled = data.enrollment != 0L
            courseContinueButton.isVisible = isEnrolled
            courseButtonSeparator.isVisible = isEnrolled
            courseDescription.isVisible = !isEnrolled
            if (!isEnrolled) {
                courseDescription.text = HtmlCompat.fromHtml(data.summary ?: "", HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
                courseDescription.doOnGlobalLayout { it.post { it.maxLines = it.height / it.lineHeight } }
            }

            adaptiveCourseMarker.isVisible = adaptiveCoursesResolver.isAdaptive(data.id)

            coursePropertiesDelegate.setStats(data)
        }
    }
}