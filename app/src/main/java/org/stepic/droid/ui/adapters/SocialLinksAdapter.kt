package org.stepic.droid.ui.adapters

import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.social_item.view.*
import org.stepic.droid.R
import org.stepic.droid.social.SocialMedia

class SocialLinksAdapter(
    private val socialLinks: Array<SocialMedia> = SocialMedia.values(),
    private val onClick: (SocialMedia) -> Unit
) : RecyclerView.Adapter<SocialLinksAdapter.SocialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialViewHolder =
        SocialViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.social_item, parent, false))

    override fun getItemCount(): Int = socialLinks.size

    override fun onBindViewHolder(holder: SocialViewHolder, position: Int) {
        val socialType = socialLinks[position]
        holder.image.setImageDrawable(AppCompatResources.getDrawable(holder.itemView.context, socialType.drawable))
        holder.itemView.setOnClickListener { onClick(socialType)}
    }

    inner class SocialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.social_item
    }
}