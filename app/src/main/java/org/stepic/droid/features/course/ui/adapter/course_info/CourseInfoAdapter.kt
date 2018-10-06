package org.stepic.droid.features.course.ui.adapter.course_info

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_course_info_block.view.*
import org.stepic.droid.features.course.ui.adapter.course_info.delegates.CourseInfoInstructorsDelegate
import org.stepic.droid.features.course.ui.adapter.course_info.delegates.CourseInfoOrganizationDelegate
import org.stepic.droid.features.course.ui.adapter.course_info.delegates.CourseInfoTextBlockDelegate
import org.stepic.droid.features.course.ui.adapter.course_info.delegates.CourseInfoVideoBlockDelegate
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoItem
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter

class CourseInfoAdapter : DelegateAdapter<CourseInfoItem, CourseInfoAdapter.ViewHolder>() {
    private var blocks : List<CourseInfoItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        addDelegate(CourseInfoTextBlockDelegate(this))
        addDelegate(CourseInfoInstructorsDelegate(this))
        addDelegate(CourseInfoVideoBlockDelegate(this, null))
        addDelegate(CourseInfoOrganizationDelegate(this))
    }

    fun setData(unsortedBlocks: List<CourseInfoItem>) {
        blocks = unsortedBlocks.sorted()
    }

    override fun getItemCount(): Int =
            blocks.size

    override fun getItemAtPosition(position: Int): CourseInfoItem =
            blocks[position]

    abstract class ViewHolder(root: View) : DelegateViewHolder<CourseInfoItem>(root)

    abstract class ViewHolderWithTitle(root: View) : ViewHolder(root) {
        protected val blockIcon: ImageView = root.blockIcon
        protected val blockTitle: TextView = root.blockTitle

        override fun onBind(data: CourseInfoItem) {
            blockIcon.setImageResource(data.type.icon)
            blockTitle.setText(data.type.title)
        }
    }
}