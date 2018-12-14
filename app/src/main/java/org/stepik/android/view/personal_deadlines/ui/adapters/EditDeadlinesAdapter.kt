package org.stepik.android.view.personal_deadlines.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.view_edit_deadlines_item.view.*
import org.stepic.droid.R
import org.stepic.droid.features.deadlines.model.Deadline
import org.stepik.android.model.Section
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.DateTimeHelper
import java.util.*

class EditDeadlinesAdapter(
        private val sections: List<Section>,
        private val deadlines: ArrayList<Deadline>,
        private val onDeadlineClicked: (Deadline) -> Unit
): RecyclerView.Adapter<EditDeadlinesAdapter.EditDeadlinesViewHolder>() {
    override fun getItemCount() = sections.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = EditDeadlinesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_edit_deadlines_item, parent, false)
    )

    override fun onBindViewHolder(holder: EditDeadlinesViewHolder, position: Int) {
        holder.sectionTitle.text = holder.itemView.context.getString(R.string.section_title_with_number,
                position + 1, sections[position].title)

        val deadline = getDeadlineForPositionOrNull(position)
        if (deadline != null) {
            holder.deadline.text = holder.itemView.context.getString(R.string.deadlines_section,
                    DateTimeHelper.getPrintableDate(deadline.deadline, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault()))
            holder.deadline.changeVisibility(true)
        } else {
            holder.deadline.changeVisibility(false)
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

    inner class EditDeadlinesViewHolder(view: View): RecyclerView.ViewHolder(view) {
        internal val sectionTitle: TextView = view.sectionTitle
        internal val deadline: TextView = view.deadline

        init {
            view.setOnClickListener { onItemClicked(adapterPosition) }
        }
    }
}