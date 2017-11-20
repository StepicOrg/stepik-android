package org.stepic.droid.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.tag_item.view.*
import org.stepic.droid.R
import org.stepic.droid.model.Tag

class TagsAdapter() : RecyclerView.Adapter<TagsAdapter.TagViewHolder>() {

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
        return TagViewHolder(tagView)
    }

    class TagViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val tagTextView = itemView.tagTextView

        fun bindTag(tag: Tag) {
            tagTextView.text = tag.title
        }

    }

}
