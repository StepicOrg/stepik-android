package org.stepik.android.view.course_info.ui.adapter.delegates.instructors

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.model.user.User
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseInfoInstructorPlaceholderAdapterDelegate : AdapterDelegate<User?, DelegateViewHolder<User?>>() {
    override fun isForViewType(position: Int, data: User?): Boolean =
        data == null

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<User?> =
        ViewHolder(createView(parent, R.layout.view_course_info_instructor_item_placeholder))

    private class ViewHolder(view: View) : DelegateViewHolder<User?>(view)
}