package org.stepik.android.view.personal_deadlines.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewLearningRateBinding
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.personal_deadlines.model.LearningRate
import ru.nobird.android.view.base.ui.extension.inflate

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
        holder.viewBinding.title.setText(rate.title)
        holder.viewBinding.icon.setImageResource(rate.icon)

        val hours = rate.millisPerWeek / AppConstants.MILLIS_IN_1HOUR
        holder.viewBinding.rate.text = hours.toString()
    }

    private fun onItemClicked(position: Int) {
        onRateClicked(rates[position])
    }

    inner class LearningRateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewBinding: ViewLearningRateBinding by viewBinding { ViewLearningRateBinding.bind(view) }

        init {
            view.setOnClickListener { onItemClicked(adapterPosition) }
        }
    }
}
