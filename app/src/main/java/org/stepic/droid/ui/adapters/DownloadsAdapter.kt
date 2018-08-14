package org.stepic.droid.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.stepic.droid.core.presenters.DownloadsPresenter
import org.stepic.droid.persistence.model.DownloadItem

class DownloadsAdapter(
        private val downloadsPresenter: DownloadsPresenter
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val activeDownloads = mutableListOf<DownloadItem>()
    private val completedDownloads = mutableListOf<DownloadItem>()

    fun addCompletedDownload(item: DownloadItem) {
        var index = completedDownloads.indexOf(item)
        if (index != -1) {
            completedDownloads[index] = item
            notifyItemChanged(index + activeDownloads.size + getTitleSize(activeDownloads.size) + getTitleSize(completedDownloads.size))
            return
        }

        index = activeDownloads.indexOf(item)
        if (index != -1) {
            removeFromDownloads(activeDownloads, 0, index)
        }

        addToDownloads(completedDownloads, activeDownloads.size + getTitleSize(activeDownloads.size), item)
    }

    fun addActiveDownload(item: DownloadItem) {
        var index = activeDownloads.indexOf(item)
        if (index != -1) {
            activeDownloads[index] = item
            notifyItemChanged(index + getTitleSize(activeDownloads.size))
            return
        }

        index = completedDownloads.indexOf(item)
        if (index != -1) {
            removeFromDownloads(completedDownloads, activeDownloads.size + getTitleSize(activeDownloads.size), index)
        }

        addToDownloads(activeDownloads, 0, item)
    }

    fun removeDownload(item: DownloadItem) {
        var index = activeDownloads.indexOf(item)
        if (index != -1) {
            removeFromDownloads(completedDownloads, 0, index)
            return
        }

        index = completedDownloads.indexOf(item)
        if (index != -1) {
            removeFromDownloads(completedDownloads, activeDownloads.size + getTitleSize(activeDownloads.size), index)
        }
    }

    private fun addToDownloads(list: MutableList<DownloadItem>, offset: Int, item: DownloadItem) {
        val wasEmpty = list.isEmpty()
        list.add(item)

        if (wasEmpty) {
            notifyItemInserted(offset)
        }

        notifyItemInserted(offset + list.size)
    }

    private fun removeFromDownloads(list: MutableList<DownloadItem>, offset: Int, index: Int) {
        list.removeAt(index)
        notifyItemRemoved(offset + index + 1) // remove element
        if (list.isEmpty()) {
            notifyItemRemoved(offset) // remove title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int =
            activeDownloads.size + getTitleSize(activeDownloads.size) +
            completedDownloads.size + getTitleSize(completedDownloads.size)

    private fun getTitleSize(downloadsSize: Int) =
            if (downloadsSize == 0) 0 else 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}