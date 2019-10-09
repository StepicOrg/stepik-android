package org.stepik.android.view.personal_deadlines.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_learning_rate.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.inflate
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.personal_deadlines.model.LearningRate

class LearningRateAdapter(
    private val rates: Array<LearningRate>,
    private val onRateClicked: (LearningRate) -> Unit
) : RecyclerView.Adapter<LearningRateAdapter.LearningRateViewHolder>() {
    override fun getItemCount(): Int =
        rates.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearningRateAdapter.LearningRateViewHolder =
        LearningRateViewHolder(parent.inflate(R.layout.view_learning_rate))

    override fun onBindViewHolder(holder: LearningRateViewHolder, position: Int) {
        val rate = rates[position]
        holder.title.setText(rate.title)
        holder.icon.setImageResource(rate.icon)

        val hours = rate.millisPerWeek / AppConstants.MILLIS_IN_1HOUR
        holder.rate.text = hours.toString()
    }

    private fun onItemClicked(position: Int) {
        onRateClicked(rates[position])
    }

    inner class LearningRateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val title: TextView = view.title
        internal val icon: ImageView = view.icon
        internal val rate: TextView = view.rate

        init {
            view.setOnClickListener { onItemClicked(adapterPosition) }
        }
    }
}