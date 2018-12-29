package org.stepik.android.view.course_info.ui.adapter

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_info_instructor_item.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.wrapWithGlide
import org.stepik.android.model.user.User

class CourseInfoInstructorsAdapter(
    private val onInstructorClicked: ((User) -> Unit)? = null
) : RecyclerView.Adapter<DelegateViewHolder<User?>>() {
    companion object {
        private const val INSTRUCTOR_VIEW_TYPE = 1
        private const val PLACEHOLDER_VIEW_TYPE = 2
    }

    var instructors: List<User?> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int =
        if (instructors[position] != null) {
            INSTRUCTOR_VIEW_TYPE
        } else {
            PLACEHOLDER_VIEW_TYPE
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DelegateViewHolder<User?> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            INSTRUCTOR_VIEW_TYPE ->
                InstructorViewHolder(inflater.inflate(R.layout.view_course_info_instructor_item, parent, false))
            PLACEHOLDER_VIEW_TYPE ->
                InstructorViewHolderPlaceholder(inflater.inflate(R.layout.view_course_info_instructor_item_placeholder, parent, false))
            else ->
                throw IllegalStateException("viewType = $viewType is unsupported")
        }
    }

    override fun getItemCount(): Int =
            instructors.size

    override fun onBindViewHolder(holder: DelegateViewHolder<User?>, position: Int) {
        holder.bind(instructors[position])
    }

    inner class InstructorViewHolder(root: View) : DelegateViewHolder<User?>(root) {
        private val instructorIcon = root.instructorIcon
        private val instructorTitle = root.instructorTitle
        private val instructorDescription = root.instructorDescription

        private val instructorIconPlaceholder =
            ContextCompat.getDrawable(context, R.drawable.general_placeholder)

        private val instructorIconWrapper = instructorIcon.wrapWithGlide()

        init {
            if (onInstructorClicked != null) {
                root.setOnClickListener { itemData?.let(onInstructorClicked) }
            }
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
                instructorDescription.changeVisibility(!data.shortBio.isNullOrBlank())
            }
        }
    }

    class InstructorViewHolderPlaceholder(root: View) : DelegateViewHolder<User?>(root)
}