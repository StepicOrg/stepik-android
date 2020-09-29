package org.stepik.android.view.step_quiz_review.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_review_status.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.inflate
import org.stepic.droid.util.resolveColorAttribute

class ReviewStatusView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val colorActive = AppCompatResources.getColorStateList(context, R.color.color_peer_review_step)
    private val colorError = ColorStateList.valueOf(context.resolveColorAttribute(R.attr.colorError))

    private val drawableActive = R.drawable.bg_peer_review_step_active
    private val drawableError = R.drawable.bg_peer_review_step_error

    private val textView: TextView
    private val imageView: View

    var status = Status.PENDING
        set(value) {
            field = value
            imageView.isVisible = value == Status.COMPLETED
            textView.isVisible = value != Status.COMPLETED

            val (color, drawableRes) =
                if (value == Status.ERROR) {
                    colorActive to drawableActive
                } else {
                    colorError to drawableError
                }

            textView.setTextColor(color)
            textView.setBackgroundResource(drawableRes)

            textView.isEnabled = value != Status.PENDING
        }

    var position: Int = 1
        set(value) {
            field = value
            textView.text = value.toString()
        }

    init {
        val view = inflate(R.layout.view_review_status, true)

        textView = view.peerReviewStatusText
        imageView = view.peerReviewStatusImage

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ReviewStatusView)
        try {
            position = typedArray.getInteger(R.styleable.ReviewStatusView_position, 1)
        } finally {
            typedArray.recycle()
        }
    }

    enum class Status {
        PENDING, IN_PROGRESS, ERROR, COMPLETED
    }
}