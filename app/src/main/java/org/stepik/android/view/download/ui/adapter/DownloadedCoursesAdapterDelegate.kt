package org.stepik.android.view.download.ui.adapter

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.downloaded_course_item.view.*
import org.stepic.droid.R
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

        init {
            root.setOnClickListener { onItemClick(itemData as Course) }
        }

        override fun onBind(data: Course) {
            downloadedCourseTitle.text = data.title

            Glide.with(context)
                .asBitmap()
                .load(data.cover)
                .placeholder(R.drawable.general_placeholder)
                .fitCenter()
                .into(downloadedCourseImage)
        }
    }
}