package org.stepic.droid.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_single_choice.view.*
import org.stepic.droid.R
import kotlin.properties.Delegates

class SingleChoiceAdapter(
        private val onItemClicked: (Int) -> Unit
): RecyclerView.Adapter<SingleChoiceAdapter.SingleChoiceViewHolder>() {
    var data: List<String> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    var selection by Delegates.observable(-1) { _, old, new ->
        if (old != -1) notifyItemChanged(old)
        if (new != -1) notifyItemChanged(new)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SingleChoiceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_single_choice, parent, false))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: SingleChoiceViewHolder, position: Int) {
        holder.text.text = data[position]
        holder.icon.setImageResource(if (selection == position) R.drawable.ic_radio_button_filled else R.drawable.ic_radio_button)
    }

    inner class SingleChoiceViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val icon: ImageView = root.radioIcon
        val text: TextView = root.radioText

        init {
            root.setOnClickListener { onItemClicked(adapterPosition) }
        }
    }
}