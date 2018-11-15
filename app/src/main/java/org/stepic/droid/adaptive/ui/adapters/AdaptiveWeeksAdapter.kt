package org.stepic.droid.adaptive.ui.adapters

import android.content.res.Resources
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.adaptive_header_stats.view.*
import kotlinx.android.synthetic.main.adaptive_item_week.view.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.model.AdaptiveWeekProgress
import org.stepic.droid.util.defaultLocale
import java.util.ArrayList

class AdaptiveWeeksAdapter : RecyclerView.Adapter<AdaptiveWeeksAdapter.StatsViewHolder>() {
    class Header(var total: Long = 0, var level: Long = 0, var last7Days: Long = 0, var chartData: LineDataSet? = null)

    private companion object {
        private const val HEADER_VIEW_TYPE = 1
        private const val ITEM_VIEW_TYPE = 2

        private const val DATE_FORMAT = "%1\$td %1\$tB %1\$tY"
    }

    private val weeks = ArrayList<AdaptiveWeekProgress>()
    private val header = Header()

    fun setHeaderLevelAndTotal(level: Long, total: Long) {
        header.level = level
        header.total = total
        notifyItemChanged(0)
    }

    fun setHeaderChart(chartData: LineDataSet?, last7Days: Long) {
        header.chartData = chartData
        header.last7Days = last7Days
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int) =
            if (position == 0)
                HEADER_VIEW_TYPE
            else
                ITEM_VIEW_TYPE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : StatsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == HEADER_VIEW_TYPE) {
            StatsViewHolder.StatsHeaderViewHolder(inflater.inflate(R.layout.adaptive_header_stats, parent, false))
        } else {
            StatsViewHolder.WeekViewHolder(inflater.inflate(R.layout.adaptive_item_week, parent, false))
        }
    }

    override fun onBindViewHolder(holder: StatsViewHolder?, p: Int) {
        holder?.let {
            when (it) {
                is StatsViewHolder.WeekViewHolder -> {
                    it.total.text = weeks[p - 1].total.toString()
                    it.start.text = String.format(Resources.getSystem().configuration.defaultLocale, DATE_FORMAT, weeks[p - 1].start)
                    it.end.text = String.format(Resources.getSystem().configuration.defaultLocale, DATE_FORMAT, weeks[p - 1].end)
                }
                is StatsViewHolder.StatsHeaderViewHolder -> {
                    header.chartData?.let { dataSet ->
                        dataSet.color = ContextCompat.getColor(it.root.context, R.color.new_accent_color)
                        dataSet.setDrawCircles(false)
                        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                        dataSet.cubicIntensity = 0.2f
                        dataSet.fillColor = dataSet.color
                        dataSet.fillAlpha = 100
                        dataSet.setDrawValues(true)
                        dataSet.setValueFormatter { v, _, _, _ -> v.toLong().toString() }
                        dataSet.valueTextSize = 12f
                        dataSet.setDrawHorizontalHighlightIndicator(false)

                        dataSet.setDrawCircles(true)
                        dataSet.setCircleColor(dataSet.color)

                        it.chart.data = LineData(dataSet)
                        it.chart.data.isHighlightEnabled = true

                        if (dataSet.entryCount > 0) {
                            it.chart.animateY(1400)
                            it.chart.invalidate()
                            it.chart.visibility = View.VISIBLE
                        } else {
                            it.chart.visibility = View.GONE
                        }
                    }

                    it.chart.visibility = if (header.chartData == null) View.INVISIBLE else View.VISIBLE

                    it.expTotal.text = header.total.toString()
                    it.level.text = header.level.toString()
                    it.expThisWeek.text = header.last7Days.toString()
                }
            }
        }
    }

    override fun getItemCount() = weeks.size + 1

    fun addAll(data: List<AdaptiveWeekProgress>) {
        weeks.addAll(data)
        notifyDataSetChanged()
    }

    sealed class StatsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class WeekViewHolder(root: View) : StatsViewHolder(root) {
            val total: TextView = root.total
            val start: TextView = root.start
            val end: TextView = root.end
        }

        class StatsHeaderViewHolder(val root: View) : StatsViewHolder(root) {
            val chart: LineChart = root.chart
            val expTotal: TextView = root.expTotal
            val level: TextView = root.level
            val expThisWeek: TextView = root.expThisWeek

            init {
                chart.description.isEnabled = false
                chart.setTouchEnabled(false)
                chart.setScaleEnabled(false)
                chart.setPinchZoom(false)
                chart.setDrawGridBackground(false)
                chart.isDragEnabled = false

                chart.xAxis.isEnabled = false
                chart.axisLeft.isEnabled = false
                chart.axisRight.isEnabled = false

                chart.legend.isEnabled = false
            }
        }
    }
}