package org.stepik.android.view.course_info.ui.adapter.delegates.instructors

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseInfoInstructorItemBinding
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
        private val viewBinding: ViewCourseInfoInstructorItemBinding by viewBinding { ViewCourseInfoInstructorItemBinding.bind(root) }

        private val instructorIconWrapper = viewBinding.instructorIcon.wrapWithGlide()

        init {
            viewBinding.root.setOnClickListener { itemData?.let(onInstructorClicked) }
        }

        override fun onBind(data: User?) {
            if (data != null) {
                instructorIconWrapper
                    .setImagePath(
                        path = data.avatar ?: "",
                        placeholder = AppCompatResources.getDrawable(context, R.drawable.general_placeholder)
                    )

                viewBinding.instructorTitle.text = data.fullName
                viewBinding.instructorDescription.text = data.shortBio
                viewBinding.instructorDescription.isGone = data.shortBio.isNullOrBlank()
            }
        }
    }
}