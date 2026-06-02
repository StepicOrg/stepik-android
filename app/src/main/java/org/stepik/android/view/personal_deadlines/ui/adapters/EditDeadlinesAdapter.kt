package org.stepik.android.view.personal_deadlines.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewEditDeadlinesItemBinding
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.personal_deadlines.model.Deadline
import org.stepik.android.model.Section
import ru.nobird.android.view.base.ui.extension.inflate
import java.util.ArrayList
import java.util.Date
import java.util.TimeZone

class EditDeadlinesAdapter(
    private val sections: List<Section>,
    private val deadlines: ArrayList<Deadline>,
    private val onDeadlineClicked: (Deadline) -> Unit
) : RecyclerView.Adapter<EditDeadlinesAdapter.EditDeadlinesViewHolder>() {
    override fun getItemCount(): Int =
        sections.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditDeadlinesViewHolder =
        EditDeadlinesViewHolder(parent.inflate(R.layout.view_edit_deadlines_item))

    override fun onBindViewHolder(holder: EditDeadlinesViewHolder, position: Int) {
        holder.viewBinding.sectionTitle.text = holder.itemView.context.getString(R.string.section_title_with_number,
                position + 1, sections[position].title)

        val deadline = getDeadlineForPositionOrNull(position)
        if (deadline != null) {
            holder.viewBinding.deadline.text = holder.itemView.context.getString(R.string.deadlines_section,
                    DateTimeHelper.getPrintableDate(deadline.deadline, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault()))
            holder.viewBinding.deadline.isVisible = true
        } else {
            holder.viewBinding.deadline.isVisible = false
        }
    }

    fun updateDeadline(deadline: Deadline) {
        val sectionIndex = sections.indexOfFirst { it.id == deadline.section }
        if (sectionIndex != -1) {
            val index = deadlines.indexOfFirst { it.section == deadline.section }
            if (index != -1) {
                deadlines[index] = deadline
            } else {
                deadlines.add(sectionIndex, deadline)
            }
            notifyItemChanged(sectionIndex)
        }
    }

    private fun getDeadlineForPositionOrNull(position: Int): Deadline? {
        val section = sections[position]
        return deadlines.find { it.section == section.id }
    }

    private fun getDeadlineForPosition(position: Int) =
        getDeadlineForPositionOrNull(position) ?: Deadline(sections[position].id, Date(DateTimeHelper.nowUtc()))

    private fun onItemClicked(position: Int) {
        onDeadlineClicked(getDeadlineForPosition(position))
    }

    inner class EditDeadlinesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewBinding: ViewEditDeadlinesItemBinding by viewBinding { ViewEditDeadlinesItemBinding.bind(view) }

        init {
            view.setOnClickListener { onItemClicked(adapterPosition) }
        }
    }
}
