package org.stepic.droid.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.cached_video_item.view.*
import kotlinx.android.synthetic.main.cached_video_item.view.video_icon as cached_video_icon
import kotlinx.android.synthetic.main.cached_video_item.view.video_header as cached_video_header
import kotlinx.android.synthetic.main.view_download_progress_determinate.view.*
import kotlinx.android.synthetic.main.downloading_video_item.view.*
import kotlinx.android.synthetic.main.downloading_video_item.view.load_button as download_video_load_button
import kotlinx.android.synthetic.main.header_download_item.view.*
import kotlinx.android.synthetic.main.load_imageview.view.*
import org.stepic.droid.R
import org.stepic.droid.core.presenters.DownloadsPresenter
import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.hideAllChildren
import org.stepic.droid.util.TextUtil.formatBytes
import org.stepic.droid.util.ThumbnailParser

class DownloadsAdapter(
        private val downloadsPresenter: DownloadsPresenter
) : RecyclerView.Adapter<DownloadsAdapter.DownloadsViewHolder>() {
    companion object {
        private const val ACTIVE_DOWNLOAD_VIEW_TYPE = 1
        private const val COMPLETED_DOWNLOAD_VIEW_TYPE = 2
        private const val TITLE_VIEW_TYPE = 3
    }

    private val activeDownloads = mutableListOf<DownloadItem>()
    private val completedDownloads = mutableListOf<DownloadItem>()

    fun addCompletedDownload(item: DownloadItem) {
        var index = completedDownloads.binarySearch(item)
        if (index < 0) { // no need to rebind completed items
            addToDownloads(completedDownloads, activeDownloads.size + getTitleSize(activeDownloads.size), item, -index - 1)
        }

        index = activeDownloads.binarySearch(item)
        if (index > -1) {
            removeFromDownloads(activeDownloads, 0, index)
        }
    }

    fun addActiveDownload(item: DownloadItem) {
        var index = activeDownloads.binarySearch(item)
        if (index > -1) {
            val oldItem = activeDownloads[index]
            if (!oldItem.isCompletelyEquals(item)) {
                activeDownloads[index] = item
                notifyItemChanged(index + getTitleSize(activeDownloads.size))
            }
            return
        } else {
            addToDownloads(activeDownloads, 0, item, -index - 1)
        }

        index = completedDownloads.binarySearch(item)
        if (index > -1) {
            removeFromDownloads(completedDownloads, activeDownloads.size + getTitleSize(activeDownloads.size), index)
        }
    }

    fun removeDownload(item: DownloadItem) {
        var index = activeDownloads.binarySearch(item)
        if (index > -1) {
            removeFromDownloads(completedDownloads, 0, index)
            return
        }

        index = completedDownloads.binarySearch(item)
        if (index > -1) {
            removeFromDownloads(completedDownloads, activeDownloads.size + getTitleSize(activeDownloads.size), index)
        }
    }

    private fun addToDownloads(list: MutableList<DownloadItem>, offset: Int, item: DownloadItem, position: Int) {
        val wasEmpty = list.isEmpty()
        list.add(position, item)

        if (wasEmpty) {
            notifyItemInserted(offset)
        }

        notifyItemInserted(offset + position + 1)
    }

    private fun removeFromDownloads(list: MutableList<DownloadItem>, offset: Int, index: Int) {
        list.removeAt(index)
        notifyItemRemoved(offset + index + 1) // remove element
        if (list.isEmpty()) {
            notifyItemRemoved(offset) // remove title
        }
    }

    override fun getItemViewType(position: Int) = when {
        activeDownloads.isNotEmpty() && position == 0 ||
                completedDownloads.isNotEmpty() && position == activeDownloads.size + getTitleSize(activeDownloads.size) ->
            TITLE_VIEW_TYPE

        position >= activeDownloads.size + getTitleSize(activeDownloads.size) + getTitleSize(completedDownloads.size) ->
            COMPLETED_DOWNLOAD_VIEW_TYPE

        else ->
            ACTIVE_DOWNLOAD_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = LayoutInflater.from(parent?.context).let {
        when(viewType) {
            ACTIVE_DOWNLOAD_VIEW_TYPE ->
                ActiveDownloadViewHolder(it.inflate(R.layout.downloading_video_item, parent, false))

            COMPLETED_DOWNLOAD_VIEW_TYPE ->
                CompletedDownloadViewHolder(it.inflate(R.layout.cached_video_item, parent, false))

            TITLE_VIEW_TYPE ->
                TitleViewHolder(it.inflate(R.layout.header_download_item, parent, false))

            else -> throw IllegalStateException("Unknown viewType = $viewType")
        }
    }

    override fun getItemCount(): Int =
            activeDownloads.size + getTitleSize(activeDownloads.size) +
            completedDownloads.size + getTitleSize(completedDownloads.size)

    private fun getTitleSize(downloadsSize: Int) =
            if (downloadsSize == 0) 0 else 1

    override fun onBindViewHolder(holder: DownloadsViewHolder, position: Int) =
        holder.bind(position)

    private fun onRemoveAllDownloadsClicked(downloads: List<DownloadItem>) {
        // presenter.removeDownloads(downloads)
    }

    private fun onRemoveDownloadClicked(download: DownloadItem) {
        // presenter.removeDownloads(listOf(download))
    }

    private fun onItemClicked(download: DownloadItem) {
        downloadsPresenter.showVideo(download.video)
    }

    abstract class DownloadsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(position: Int)
    }


    inner class ActiveDownloadViewHolder(view: View): DownloadsViewHolder(view) {
        private val videoIcon = view.video_icon
        private val videoHeader = view.video_header
        private val progressText = view.progress_text

        private val loadButton = view.load_button
        private val progressWheel = view.when_load_view

        private var stepId = -1L

        init {
            progressWheel.setOnClickListener { onRemoveDownloadClicked(getItem(adapterPosition)) }
        }

        private fun getItem(position: Int) =
                activeDownloads[position - 1]

        override fun bind(position: Int) {
            val item = activeDownloads[position - 1]

            val needAnimation = stepId == item.step
            stepId = item.step

            val thumbnail = item.video.thumbnail
            Glide.with(itemView.context)
                    .load(thumbnail?.let(ThumbnailParser::getUriForThumbnail))
                    .placeholder(R.drawable.video_placeholder)
                    .into(videoIcon)

            videoHeader.text = item.title

            progressText.text = "${formatBytes(item.bytesDownloaded)} / ${formatBytes(item.bytesTotal)}"

            loadButton.hideAllChildren()
            val progress = if (item.bytesTotal == 0L) {
                0f
            } else {
                item.bytesDownloaded.toFloat() / item.bytesTotal
            }

            progressWheel.changeVisibility(true)
            progressWheel.setProgressPortion(progress, needAnimation)
        }
    }


    inner class CompletedDownloadViewHolder(view: View): DownloadsViewHolder(view) {
        private val videoIcon = view.cached_video_icon
        private val videoHeader = view.cached_video_header
        private val cachedBytes = view.size_of_cached_video

        private val loadButton = view.load_button

        private val deleteIcon = view.after_load_iv

        init {
            deleteIcon.setOnClickListener { onRemoveDownloadClicked(getItem(adapterPosition)) }
            view.setOnClickListener { onItemClicked(getItem(adapterPosition)) }
        }

        private fun getItem(position: Int) =
                completedDownloads[position - activeDownloads.size - getTitleSize(activeDownloads.size) - 1]

        override fun bind(position: Int) {
            val item = getItem(position)

            val thumbnail = item.video.thumbnail
            Glide.with(itemView.context)
                    .load(thumbnail?.let(ThumbnailParser::getUriForThumbnail))
                    .placeholder(R.drawable.video_placeholder)
                    .into(videoIcon)

            videoHeader.text = item.title

            cachedBytes.text = formatBytes(item.bytesTotal)

            loadButton.hideAllChildren()
            deleteIcon.changeVisibility(true)
        }
    }


    inner class TitleViewHolder(view: View): DownloadsViewHolder(view) {
        private val headerText = view.headerText
        private val headerButton = view.headerButton

        init {
            headerButton.setOnClickListener {
                onRemoveAllDownloadsClicked(if (adapterPosition == 0 && activeDownloads.isNotEmpty()) {
                    activeDownloads
                } else {
                    completedDownloads
                })
            }
        }

        override fun bind(position: Int) {
            if (position == 0 && activeDownloads.isNotEmpty()) {
                headerText.setText(R.string.downloading_title)
                headerButton.setText(R.string.downloading_cancel_all)
            } else {
                headerText.setText(R.string.cached_title)
                headerButton.setText(R.string.remove_all)
            }
        }
    }
}