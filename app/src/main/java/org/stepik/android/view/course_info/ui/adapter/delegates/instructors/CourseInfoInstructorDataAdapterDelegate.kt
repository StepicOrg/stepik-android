package org.stepik.android.view.course_info.ui.adapter.delegates.instructors

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.view_course_info_instructor_item.view.*
import org.stepic.droid.R
import org.stepik.android.view.glide.ui.extension.wrapWithGlide
import org.stepik.android.model.user.User
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseInfoInstructorDataAdapterDelegate(
    private val onInstructorClicked: (User) -> Unit
) : AdapterDelegate<User?, DelegateViewHolder<User?>>() {
    override fun isForViewType(position: Int, data: User?): Boolean =
        data != null

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<User?> =
        ViewHolder(createView(parent, R.layout.view_course_info_instructor_item))

    private inner class ViewHolder(root: View) : DelegateViewHolder<User?>(root) {
        private val instructorIcon = root.instructorIcon
        private val instructorTitle = root.instructorTitle
        private val instructorDescription = root.instructorDescription

        private val instructorIconPlaceholder =
            ContextCompat.getDrawable(context, R.drawable.general_placeholder)

        private val instructorIconWrapper = instructorIcon.wrapWithGlide()

        init {
            root.setOnClickListener { itemData?.let(onInstructorClicked) }
        }

        override fun onBind(data: User?) {
            if (data != null) {
                instructorIconWrapper
                    .setImagePath(
                        path = data.avatar ?: "",
                        placeholder = instructorIconPlaceholder
                    )

                instructorTitle.text = data.fullName
                instructorDescription.text = data.shortBio
                instructorDescription.isGone = data.shortBio.isNullOrBlank()
            }
        }
    }
}