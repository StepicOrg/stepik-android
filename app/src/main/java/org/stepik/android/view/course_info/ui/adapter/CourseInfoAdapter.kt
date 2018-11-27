package org.stepik.android.view.course_info.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_course_info_block.view.*
import org.stepic.droid.fonts.FontsProvider
import org.stepik.android.view.course_info.ui.adapter.delegates.CourseInfoInstructorsDelegate
import org.stepik.android.view.course_info.ui.adapter.delegates.CourseInfoOrganizationDelegate
import org.stepik.android.view.course_info.ui.adapter.delegates.CourseInfoTextBlockDelegate
import org.stepik.android.view.course_info.ui.adapter.delegates.CourseInfoVideoBlockDelegate
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter

class CourseInfoAdapter(
    fontsProvider: FontsProvider
) : DelegateAdapter<CourseInfoItem, CourseInfoAdapter.ViewHolder>() {
    private var blocks : List<CourseInfoItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        addDelegate(CourseInfoTextBlockDelegate(this, fontsProvider))
        addDelegate(CourseInfoInstructorsDelegate(this))
        addDelegate(CourseInfoVideoBlockDelegate(this, null))
        addDelegate(CourseInfoOrganizationDelegate(this))
    }

    fun setSortedData(sortedBlocks: List<CourseInfoItem>) {
        blocks = sortedBlocks
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