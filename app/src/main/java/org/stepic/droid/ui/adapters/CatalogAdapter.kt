package org.stepic.droid.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.catalog_item.view.*
import org.stepic.droid.R
import org.stepic.droid.model.CourseListItem

class CatalogAdapter(
        private val courseListItems: List<CourseListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val CAROUSEL_TYPE = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CAROUSEL_TYPE -> {
                val view = layoutInflater.inflate(R.layout.catalog_item, parent, false)
                CarouselViewHolder(view)
            }
            else -> throw IllegalStateException("CatalogAdapter viewType = $viewType is unsupported")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            CAROUSEL_TYPE -> (holder as CarouselViewHolder).bindData(courseListItemBy(adapterPosition = position))
        }
    }

    private fun courseListItemBy(adapterPosition: Int): CourseListItem = courseListItems[adapterPosition]

    override fun getItemCount(): Int = courseListItems.size

    override fun getItemViewType(position: Int): Int = CAROUSEL_TYPE

    private class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val coursesCarousel = itemView.coursesCarouselItem

        fun bindData(courseListItem: CourseListItem) {


        }

    }
}
