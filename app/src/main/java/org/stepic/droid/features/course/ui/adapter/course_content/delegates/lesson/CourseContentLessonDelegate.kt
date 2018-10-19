package org.stepic.droid.features.course.ui.adapter.course_content.delegates.lesson

import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_course_content_lesson.view.*
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget

class CourseContentLessonDelegate(
        adapter: DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>,
        private val lessonClickListener: CourseContentLessonClickListener
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>(adapter) {

    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_content_lesson))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) is CourseContentItem.LessonItem

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        private val lessonIcon = root.lessonIcon
        private val lessonTitle = root.lessonTitle
        private val lessonTextProgress = root.lessonTextProgress
        private val lessonProgress = root.lessonProgress

        private val lessonIconTarget = RoundedBitmapImageViewTarget(
                context.resources.getDimension(R.dimen.course_image_radius), lessonIcon)

        private val lessonIconPlaceholder = with(context.resources) {
            val coursePlaceholderBitmap = BitmapFactory.decodeResource(this, R.drawable.general_placeholder)
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(this, coursePlaceholderBitmap)
            circularBitmapDrawable.cornerRadius = getDimension(R.dimen.course_image_radius)
            circularBitmapDrawable
        }

        init {
            root.setOnClickListener {
                (itemData as? CourseContentItem.LessonItem)?.let(lessonClickListener::onItemClicked)
            }
        }

        override fun onBind(data: CourseContentItem) {
            with(data as CourseContentItem.LessonItem) {
                lessonTitle.text = context.resources.getString(R.string.course_content_lesson_title,
                        section.position, unit.position, lesson.title)

                lessonTextProgress.text = context.resources.getString(R.string.course_content_text_progress,
                        progress.nStepsPassed, progress.nSteps)

                lessonProgress.progress = progress.nStepsPassed.toFloat() / progress.nSteps

                Glide.with(lessonIcon.context)
                        .load(lesson.coverUrl)
                        .asBitmap()
                        .placeholder(lessonIconPlaceholder)
                        .centerCrop()
                        .into(lessonIconTarget)
            }
        }
    }
}