package org.stepic.droid.ui.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.new_course_item.view.*
import org.stepic.droid.R

class CourseSimpleAdapter(private val context: Context) : RecyclerView.Adapter<CourseSimpleAdapter.Companion.SimpleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.new_course_item, parent, false)
        return SimpleViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: SimpleViewHolder, position: Int) {
        viewHolder.setData(position)
    }

    override fun getItemCount() = 11


    companion object {
        class SimpleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val coursePlaceholder by lazy {
                val coursePlaceholderBitmap = BitmapFactory.decodeResource(view.context.resources, R.drawable.general_placeholder)
                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(view.context.resources, coursePlaceholderBitmap)
                circularBitmapDrawable.cornerRadius = view.context.resources.getDimension(R.dimen.course_image_radius)
                circularBitmapDrawable
            }

            fun setData(position: Int) {
                itemView.courseItemImage.setImageDrawable(coursePlaceholder)
                itemView.courseItemName.text = "Course name $position"

            }
        }
    }
}
