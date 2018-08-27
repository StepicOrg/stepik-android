package org.stepic.droid.ui.adapters

import android.support.v4.util.LongSparseArray
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.load_imageview.view.*
import kotlinx.android.synthetic.main.unit_item.view.*
import kotlinx.android.synthetic.main.view_download_progress_determinate.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.UnitsPresenter
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepic.droid.transformers.transformToViewModel
import org.stepic.droid.ui.custom.progressbutton.ProgressWheel
import org.stepic.droid.ui.fragments.UnitsFragment
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.hideAllChildren
import org.stepik.android.model.Lesson
import org.stepik.android.model.Progress
import org.stepik.android.model.Section
import org.stepik.android.model.Unit

class UnitAdapter(
        var parentSection: Section?,

        private val analytic: Analytic,
        private val unitsFragment: UnitsFragment,
        private val unitsPresenter: UnitsPresenter
): RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {
    val units = mutableListOf<Unit>()
    val lessons = mutableListOf<Lesson>()
    val unitProgressMap = LongSparseArray<Progress>()

    private val downloadProgresses = LongSparseArray<DownloadProgress.Status>()

    override fun getItemCount(): Int = units.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            UnitViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.unit_item, parent, false))

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val lesson = lessons[position]
        val unit = units[position]

        holder.apply {
            val title = "${parentSection?.position?.toString()?.plus(".") ?: ""}${unit.position} ${lesson.title}"
            unitTitle.text = title

            Glide.with(App.getAppContext())
                    .load(lesson.coverUrl)
                    .placeholder(R.drawable.general_placeholder)
                    .into(holder.lessonIcon)

            val progress = unitProgressMap[unit.id]
            val progressViewModel = try {
                progress.transformToViewModel()
            } catch (_: Exception) {
                null
            }

            val needShowLearningProgress = (progressViewModel?.cost ?: 0) > 0
            progressViewModel?.let {
                textScore.text = it.scoreAndCostText
                progressScore.max = it.cost
                progressScore.progress = it.score
            }
            textScore.changeVisibility(needShowLearningProgress)
            progressScore.changeVisibility(needShowLearningProgress)

            viewedItem.changeVisibility(unit.is_viewed_custom)

            // download progress
            loadButton.hideAllChildren()
            val downloadProgressStatus: DownloadProgress.Status? = downloadProgresses[unit.id]

            when(downloadProgressStatus) {
                is DownloadProgress.Status.Cached ->
                    afterLoad.changeVisibility(true)

                is DownloadProgress.Status.NotCached ->
                    preLoadIV.changeVisibility(true)

                is DownloadProgress.Status.InProgress -> {
                    whenLoad.changeVisibility(true)
                    whenLoad.setProgressPortion(downloadProgressStatus.progress, oldLessonId == lesson.id)
                    oldLessonId = lesson.id
                }

                else ->
                    loadStateUndefined.changeVisibility(true)
            }
        }
    }

    fun setItemDownloadProgress(progress: DownloadProgress) {
        downloadProgresses.put(progress.id, progress.status)
        val pos = units.indexOfFirst { it.id == progress.id }
        if (pos != -1) {
            notifyItemChanged(pos)
        }
    }

    private fun onItemClicked(pos: Int) {
        if (pos in units.indices) {
            unitsFragment.openSteps(units[pos], lessons[pos], parentSection)
        }
    }

    fun onItemDownloadClicked(pos: Int) {
        if (pos in units.indices) {
            unitsPresenter.addDownloadTask(pos)
        }
    }

    fun onItemRemoveClicked(pos: Int) {
        if (pos in units.indices) {
            unitsPresenter.removeDownloadTask(pos)
        }
    }

    inner class UnitViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val cv: View = root.cv
        val unitTitle: TextView = root.unit_title
        val preLoadIV: View = root.pre_load_iv
        val whenLoad: ProgressWheel = root.when_load_view
        val afterLoad: View = root.after_load_iv
        val loadButton: ViewGroup = root.load_button
        val viewedItem: View = root.viewed_item
        val textScore: TextView = root.text_score
        val progressScore: ProgressBar = root.student_progress_score_bar
        val lessonIcon: ImageView = root.lesson_icon
        val loadStateUndefined: View = root.loadStateUndefined

        var oldLessonId = -1L

        init {
            root.setOnClickListener {
                onItemClicked(adapterPosition)
            }

            preLoadIV.setOnClickListener {
                onItemDownloadClicked(adapterPosition)
                whenLoad.setProgressPortion(0f, false)

                analytic.reportEvent(Analytic.Interaction.CLICK_CACHE_LESSON, units[adapterPosition].id.toString())
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.Downloads.STARTED,
                        mapOf(AmplitudeAnalytic.Downloads.PARAM_CONTENT to AmplitudeAnalytic.Downloads.Values.LESSON))
            }

            whenLoad.setOnClickListener {
                onItemRemoveClicked(adapterPosition)

                analytic.reportEvent(Analytic.Interaction.CLICK_CANCEL_LESSON, units[adapterPosition].id.toString())
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.Downloads.CANCELLED,
                        mapOf(AmplitudeAnalytic.Downloads.PARAM_CONTENT to AmplitudeAnalytic.Downloads.Values.LESSON))
            }

            afterLoad.setOnClickListener {
                onItemRemoveClicked(adapterPosition)

                analytic.reportEvent(Analytic.Interaction.CLICK_DELETE_LESSON, units[adapterPosition].id.toString())
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.Downloads.DELETED,
                        mapOf(AmplitudeAnalytic.Downloads.PARAM_CONTENT to AmplitudeAnalytic.Downloads.Values.LESSON))
            }
        }
    }
}