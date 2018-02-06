package org.stepic.droid.adaptive.ui.custom.morphing

import android.widget.TextView
import org.stepic.droid.R

object MorphingHelper {

    @JvmStatic
    fun morphStreakHeaderToIncBubble(header: MorphingView, inc: TextView) =
            MorphingAnimation(header, MorphingView.MorphParams(
                    cornerRadius = header.context.resources.getDimension(R.dimen.adaptive_exp_bubble_corner_radius),

                    marginRight = header.context.resources.getDimension(R.dimen.adaptive_exp_bubble_margin).toInt(),

                    width = inc.width,
                    height = inc.height,

                    textSize = inc.textSize,
                    text = inc.text.toString()))

}