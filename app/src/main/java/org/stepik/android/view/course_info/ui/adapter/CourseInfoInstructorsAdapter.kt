package org.stepik.android.view.course_info.ui.adapter

import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_course_info_intstructor_item.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepik.android.model.user.User

class CourseInfoInstructorsAdapter : RecyclerView.Adapter<CourseInfoInstructorsAdapter.InstructorViewHolder>() {
    var instructors: List<User> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructorViewHolder =
            InstructorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_course_info_intstructor_item, parent, false))

    override fun getItemCount(): Int =
            instructors.size

    override fun onBindViewHolder(holder: InstructorViewHolder, position: Int) {
        holder.bind(instructors[position])
    }

    class InstructorViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val instructorIcon = root.instructorIcon
        private val instructorTitle = root.instructorTitle
        private val instructorDescription = root.instructorDescription

        private val instructorIconPlaceholder by lazy {
            val resources = root.context.resources
            val coursePlaceholderBitmap = BitmapFactory.decodeResource(resources, R.drawable.general_placeholder)
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, coursePlaceholderBitmap)
            circularBitmapDrawable.cornerRadius = resources.getDimension(R.dimen.course_image_radius)
            circularBitmapDrawable
        }

        private val instructorIconTarget by lazy {
            RoundedBitmapImageViewTarget(root.context.resources.getDimension(R.dimen.course_image_radius), instructorIcon)
        }

        fun bind(instructor: User) {
            Glide.with(itemView.context)
                    .load(instructor.avatar)
                    .asBitmap()
                    .placeholder(instructorIconPlaceholder)
                    .centerCrop()
                    .into(instructorIconTarget)

            instructorTitle.text = instructor.fullName
            instructorDescription.text = instructor.shortBio
        }
    }
}