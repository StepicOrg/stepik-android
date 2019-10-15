package org.stepic.droid.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tag_item.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.listeners.OnItemClickListener
import org.stepik.android.model.Tag

class TagsAdapter(private val onTagClicked: (Tag) -> Unit) : RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {

    private val _tags: MutableList<Tag> = mutableListOf()

    fun setTags(tags: List<Tag>) {
        _tags.clear()
        _tags.addAll(tags)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bindTag(_tags[position])
    }

    override fun getItemCount(): Int = _tags.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val tagView = layoutInflater.inflate(R.layout.tag_item, parent, false)
        val onItemClickListener: OnItemClickListener = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                onItemClicked(position)
            }
        }
        return TagViewHolder(tagView, onItemClickListener)
    }

    private fun onItemClicked(adapterPosition: Int) {
        if (adapterPosition > _tags.size || adapterPosition < 0) {
            return
        }
        val tag = _tags[adapterPosition]
        onTagClicked.invoke(tag)
    }

    class TagViewHolder(view: View, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(view) {

        private val tagTextView = itemView.tagTextView

        init {
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
        }

        fun bindTag(tag: Tag) {
            tagTextView.text = tag.title
        }

    }

}
