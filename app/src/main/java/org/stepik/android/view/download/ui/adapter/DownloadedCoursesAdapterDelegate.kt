package org.stepik.android.view.download.ui.adapter

import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.downloaded_course_item.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepik.android.model.Course
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class DownloadedCoursesAdapterDelegate(
    private val onItemClick: (Course) -> Unit
) : AdapterDelegate<Course, DelegateViewHolder<Course>>() {
    override fun isForViewType(position: Int, data: Course): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<Course> =
        ViewHolder(createView(parent, R.layout.downloaded_course_item))

    private inner class ViewHolder(root: View): DelegateViewHolder<Course>(root) {

        private val downloadedCourseTitle = root.downloadedCourseName
        private val downloadedCourseImage = root.downloadedCourseImage

        private val coursePlaceholderBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.general_placeholder
        )

        private val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(
            context.resources,
            coursePlaceholderBitmap
        )


        private val imageViewTarget: BitmapImageViewTarget =  RoundedBitmapImageViewTarget(itemView.resources.getDimension(R.dimen.course_image_radius), downloadedCourseImage)

        init {
            root.setOnClickListener { onItemClick(itemData as Course) }
            circularBitmapDrawable.cornerRadius = context.resources.getDimension(R.dimen.course_image_radius)
        }

        override fun onBind(data: Course) {
            downloadedCourseTitle.text = data.title

            Glide.with(context)
                .asBitmap()
                .load(data.cover)
                .placeholder(circularBitmapDrawable)
                .fitCenter()
                .into(imageViewTarget)
        }
    }
}