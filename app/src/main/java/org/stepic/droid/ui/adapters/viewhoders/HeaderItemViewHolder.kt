package org.stepic.droid.ui.adapters.viewhoders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.model.CoursesDescriptionContainer
import org.stepic.droid.ui.custom.PlaceholderTextView
import org.stepic.droid.util.ColorUtil

class HeaderItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val placeholder = view as PlaceholderTextView

    fun bindData(descriptionContainer: CoursesDescriptionContainer) {
        placeholder.setPlaceholderText(descriptionContainer.description)
        placeholder.setBackgroundResource(descriptionContainer.colors.backgroundResSquared)
        placeholder.setTextColor(ColorUtil.getColorArgb(descriptionContainer.colors.textColorRes, placeholder.context))
    }
}