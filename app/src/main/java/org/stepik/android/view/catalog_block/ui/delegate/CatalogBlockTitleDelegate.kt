package org.stepik.android.view.catalog_block.ui.delegate

import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_user_course_list.*
import kotlinx.android.synthetic.main.view_container_block.view.*
import org.stepic.droid.R
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem

class CatalogBlockTitleDelegate(
    private val view: ViewGroup
) {
    private val title = view.courseListTitle
    private val count = view.coursesCarouselCount
    private val description = view.courseListDescription

    fun setInformation(data: CatalogBlockItem) {
        view.isVisible = data.isTitleVisible
        title.text = data.title

        description.text = data.description
        description.isVisible = data.description.isNotEmpty()
    }

    fun setCount(data: CatalogBlockItem) {
        if (data.content !is CatalogBlockContent.FullCourseList) {
            return
        }
        count.isVisible = true
        count.text = view.resources.getQuantityString(
            R.plurals.course_count,
            data.content.content.coursesCount,
            data.content.content.coursesCount
        )
    }
}