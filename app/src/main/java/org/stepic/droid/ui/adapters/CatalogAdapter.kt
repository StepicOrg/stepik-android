package org.stepic.droid.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import kotlinx.android.synthetic.main.catalog_item.view.*
import kotlinx.android.synthetic.main.view_course_languages.view.*
import org.stepic.droid.R
import org.stepic.droid.model.CollectionDescriptionColors
import org.stepic.droid.model.CoursesCarouselInfo
import org.stepic.droid.model.StepikFilter
import java.util.*

class CatalogAdapter(
        private val courseListItems: List<CoursesCarouselInfo>,
        private val onFiltersChanged: (EnumSet<StepikFilter>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val CAROUSEL_TYPE = 0
        private const val LANGUAGES_TYPE = 1

        private const val PRE_CAROUSEL_COUNT = 1
        private const val LANGUAGE_INDEX = 0
    }

    private var filters: EnumSet<StepikFilter>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CAROUSEL_TYPE -> {
                val view = layoutInflater.inflate(R.layout.catalog_item, parent, false)
                CarouselViewHolder(view)
            }
            LANGUAGES_TYPE -> {
                val view = layoutInflater.inflate(R.layout.view_course_languages, parent, false)
                LanguagesViewHolder(view)
            }
            else -> throw IllegalStateException("CatalogAdapter viewType = $viewType is unsupported")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            CAROUSEL_TYPE -> {
                holder as CarouselViewHolder
                val coursesCarouselInfo = courseListItemBy(adapterPosition = position)
                val descriptionColors = getDescriptionColors(position)
                holder.bindData(coursesCarouselInfo, descriptionColors)
            }
            LANGUAGES_TYPE -> {
                holder as LanguagesViewHolder
                holder.refreshLanguages()
            }
        }
    }

    fun showFilters(filtersFromPreferences: EnumSet<StepikFilter>) {
        filters = filtersFromPreferences
        notifyItemChanged(LANGUAGE_INDEX)
    }

    private fun getDescriptionColors(position: Int): CollectionDescriptionColors =
            when (position % 2) {
                0 -> CollectionDescriptionColors.BLUE
                1 -> CollectionDescriptionColors.FIRE
                else -> throw IllegalStateException("Use correct divider")
            }

    private fun courseListItemBy(adapterPosition: Int): CoursesCarouselInfo =
            courseListItems[adapterPosition - PRE_CAROUSEL_COUNT]

    override fun getItemCount(): Int = courseListItems.size + PRE_CAROUSEL_COUNT

    override fun getItemViewType(adapterPosition: Int): Int =
            when (adapterPosition) {
                0 -> LANGUAGES_TYPE
                in PRE_CAROUSEL_COUNT until itemCount -> CAROUSEL_TYPE
                else -> throw IllegalStateException("Catalog recycler type is not identified")
            }

    private class CarouselViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val coursesCarousel = itemView.coursesCarouselItem

        fun bindData(coursesCarouselInfo: CoursesCarouselInfo, descriptionColors: CollectionDescriptionColors) {
            coursesCarousel.setDescriptionColors(descriptionColors)
            coursesCarousel.setCourseCarouselInfo(coursesCarouselInfo)
        }

    }

    private inner class LanguagesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val languageRu = itemView.languageRu
        private val languageEn = itemView.languageEn

        init {
            val onClickListener = View.OnClickListener { checkableView ->
                checkableView as Checkable
                if (checkableView.isChecked) {
                    //skip click event
                    return@OnClickListener
                }
                languageRu.toggle()
                languageEn.toggle()
                val filters = composeFilters()
                onFiltersChanged(filters)
            }

            languageRu.setOnClickListener(onClickListener)
            languageEn.setOnClickListener(onClickListener)
        }

        fun refreshLanguages() {
            val localFilters = filters ?: return

            languageRu.isChecked = localFilters.contains(StepikFilter.RUSSIAN)
            languageEn.isChecked = localFilters.contains(StepikFilter.ENGLISH)
        }

        private fun composeFilters(): EnumSet<StepikFilter> {
            val filters = EnumSet.noneOf(StepikFilter::class.java)
            if (languageRu.isChecked) {
                filters.add(StepikFilter.RUSSIAN)
            }

            if (languageEn.isChecked) {
                filters.add(StepikFilter.ENGLISH)
            }
            return filters
        }
    }
}
