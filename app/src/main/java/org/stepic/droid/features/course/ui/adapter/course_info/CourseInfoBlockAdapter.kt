package org.stepic.droid.features.course.ui.adapter.course_info

import android.support.annotation.CallSuper
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_course_info_block.view.*
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoType

class CourseInfoBlockAdapter : RecyclerView.Adapter<CourseInfoBlockAdapter.CourseInfoViewHolder<CourseInfoBlock>>() {
    companion object {
        private const val VIEW_TYPE_TEXT_BLOCK = 1
        private const val VIEW_TYPE_INSTRUCTORS_BLOCK = 2
    }

    private var blocks : List<CourseInfoBlock> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setData(unsortedBlocks: List<CourseInfoBlock>) {
        blocks = unsortedBlocks.sorted()
    }

    override fun getItemViewType(position: Int): Int =
            when(blocks[position].type) {
                CourseInfoType.INSTRUCTORS ->
                    VIEW_TYPE_INSTRUCTORS_BLOCK

                else ->
                    VIEW_TYPE_TEXT_BLOCK
            }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CourseInfoViewHolder<CourseInfoBlock> = TODO()


    override fun getItemCount() =
            blocks.size

    override fun onBindViewHolder(holder: CourseInfoViewHolder<CourseInfoBlock>, position: Int) {}


    abstract class CourseInfoViewHolder<T : CourseInfoBlock>(root: View) : RecyclerView.ViewHolder(root) {
        protected val blockIcon: ImageView = root.blockIcon
        protected val blockTitle: TextView = root.blockTitle

        @CallSuper
        open fun onBind(data: T) {
            blockIcon.setImageResource(data.type.icon)
            blockTitle.setText(data.type.title)
        }
    }
}