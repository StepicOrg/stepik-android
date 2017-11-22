package org.stepic.droid.ui.adapters.viewhoders

import android.view.View
import org.stepic.droid.model.CoursesDescriptionContainer
import org.stepic.droid.ui.custom.PlaceholderTextView
import org.stepic.droid.util.ColorUtil

class HeaderItemViewHolder(view: View, private val descriptionContainer: CoursesDescriptionContainer): CourseViewHolderBase(view) {
    private val placeholder = view as PlaceholderTextView

    override fun setDataOnView(position: Int) {
        placeholder.setPlaceholderText(descriptionContainer.description)
        placeholder.setBackgroundResource(descriptionContainer.colors.backgroundRes)
        placeholder.setTextColor(ColorUtil.getColorArgb(descriptionContainer.colors.textColorRes, placeholder.context))
    }
}