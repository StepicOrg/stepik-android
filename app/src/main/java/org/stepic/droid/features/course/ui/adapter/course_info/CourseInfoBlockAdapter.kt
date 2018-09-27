package org.stepic.droid.features.course.ui.adapter.course_info

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_course_info_block.view.*
import kotlinx.android.synthetic.main.view_course_info_instructors_block.view.*
import kotlinx.android.synthetic.main.view_course_info_text_block.view.*
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoInstructorsBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoTextBlock
import org.stepic.droid.features.course.ui.model.course_info.CourseInfoType
import java.lang.IllegalStateException

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

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CourseInfoViewHolder<CourseInfoBlock> =
            when(viewType) {
                VIEW_TYPE_INSTRUCTORS_BLOCK ->
                    CourseInfoInstructorsBlockViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.view_course_info_instructors_block, parent, false))
                            as CourseInfoViewHolder<CourseInfoBlock>

                VIEW_TYPE_TEXT_BLOCK ->
                    CourseInfoTextBlockViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.view_course_info_text_block, parent, false))
                            as CourseInfoViewHolder<CourseInfoBlock>

                else -> throw IllegalStateException("Unknown viewType = $viewType")
            }


    override fun getItemCount() =
            blocks.size

    override fun onBindViewHolder(holder: CourseInfoViewHolder<CourseInfoBlock>, position: Int) {
        val block = blocks[position]
        when(getItemViewType(position)) {
            VIEW_TYPE_TEXT_BLOCK -> {
                holder as CourseInfoTextBlockViewHolder
                block as CourseInfoTextBlock
                holder.onBind(block)
            }

            VIEW_TYPE_INSTRUCTORS_BLOCK -> {
                holder as CourseInfoInstructorsBlockViewHolder
                block as CourseInfoInstructorsBlock
                holder.onBind(block)
            }
        }
    }

    class CourseInfoInstructorsBlockViewHolder(root: View) : CourseInfoViewHolder<CourseInfoInstructorsBlock>(root) {
        private val adapter = CourseInfoInstructorsAdapter()

        init {
            root.blockInstructors.let {
                it.adapter = adapter
                it.layoutManager = LinearLayoutManager(root.context)
            }
        }

        override fun onBind(data: CourseInfoInstructorsBlock) {
            super.onBind(data)

            adapter.instructors = data.instructors
        }
    }

    class CourseInfoTextBlockViewHolder(root: View) : CourseInfoViewHolder<CourseInfoTextBlock>(root) {
        private val blockMessage = root.blockMessage

        override fun onBind(data: CourseInfoTextBlock) {
            super.onBind(data)
            blockMessage.text = data.message
        }
    }

    abstract class CourseInfoViewHolder<T : CourseInfoBlock>(root: View) : RecyclerView.ViewHolder(root) {
        protected val blockIcon: ImageView = root.blockIcon
        protected val blockTitle: TextView = root.blockTitle

        open fun onBind(data: T) {
            blockIcon.setImageResource(data.type.icon)
            blockTitle.setText(data.type.title)
        }
    }
}