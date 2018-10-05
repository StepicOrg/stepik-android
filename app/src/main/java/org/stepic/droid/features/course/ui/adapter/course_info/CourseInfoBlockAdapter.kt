package org.stepic.droid.features.course.ui.adapter.course_info

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_course_info_block.view.*
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoBlock
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.RecyclerViewDelegateAdapter

class CourseInfoBlockAdapter : RecyclerViewDelegateAdapter<CourseInfoBlock, CourseInfoBlockAdapter.CourseInfoViewHolder<CourseInfoBlock>>() {
    private var blocks : List<CourseInfoBlock> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        addDelegate(CourseInfoTextBlockDelegate(this))
        addDelegate(CourseInfoInstructorsBlockDelegate(this))
    }

    fun setData(unsortedBlocks: List<CourseInfoBlock>) {
        blocks = unsortedBlocks.sorted()
    }

    override fun getItemCount(): Int =
            blocks.size

    override fun getItemAtPosition(position: Int): CourseInfoBlock =
            blocks[position]

    abstract class CourseInfoViewHolder<T : CourseInfoBlock>(root: View) : DelegateViewHolder<T>(root) {
        protected val blockIcon: ImageView = root.blockIcon
        protected val blockTitle: TextView = root.blockTitle

        override fun onBind(data: T) {
            blockIcon.setImageResource(data.type.icon)
            blockTitle.setText(data.type.title)
        }
    }
}