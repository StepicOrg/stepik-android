package org.stepic.droid.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.stepic.droid.R
import org.stepik.android.domain.base.PaginationDirection
import ru.nobird.android.view.base.ui.extension.setTextColor

fun View.setHeight(height: Int) {
    layoutParams.height = height
    layoutParams = layoutParams
}

fun ViewGroup.hideAllChildren() {
    children.forEach { it.isVisible = false }
}

fun TextView.setCompoundDrawables(
    @DrawableRes start: Int = -1,
    @DrawableRes top: Int = -1,
    @DrawableRes end: Int = -1,
    @DrawableRes bottom: Int = -1
) {
    fun TextView.getDrawableOrNull(@DrawableRes res: Int) =
        if (res != -1) AppCompatResources.getDrawable(context, res) else null

    val startDrawable = getDrawableOrNull(start)
    val topDrawable = getDrawableOrNull(top)
    val endDrawable = getDrawableOrNull(end)
    val bottomDrawable = getDrawableOrNull(bottom)
    setCompoundDrawablesWithIntrinsicBounds(startDrawable, topDrawable, endDrawable, bottomDrawable)
}

fun TextView.setTextViewBackgroundWithoutResettingPadding(@DrawableRes backgroundRes: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        val paddingLeft = this.paddingLeft
        val paddingTop = this.paddingTop
        val paddingRight = this.paddingRight
        val paddingBottom = this.paddingBottom
        val compoundDrawablePadding = this.compoundDrawablePadding

        setBackgroundResource(backgroundRes)
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        this.compoundDrawablePadding = compoundDrawablePadding
    } else {
        setBackgroundResource(backgroundRes)
    }
}


fun Drawable.toBitmap(width: Int = intrinsicWidth, height: Int = intrinsicHeight): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

fun ViewGroup.inflate(@LayoutRes resId: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(this.context).inflate(resId, this, attachToRoot)

inline fun <T : View> T.doOnGlobalLayout(crossinline action: (view: T) -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            action(this@doOnGlobalLayout)
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

fun View.snackbar(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_SHORT) {
    snackbar(context.getString(messageRes), length)
}

fun View.snackbar(message: String, length: Int = Snackbar.LENGTH_SHORT) {
    Snackbar
        .make(this, message, length)
        .setTextColor(ContextCompat.getColor(context, R.color.white))
        .show()
}

fun RecyclerView.setOnPaginationListener(onPagination: (PaginationDirection) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager)
                ?: return

            val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
            if (dy > 0) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount

                if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                    post { onPagination(PaginationDirection.DOWN) }
                }
            } else {
                if (pastVisibleItems == 0) {
                    post { onPagination(PaginationDirection.UP) }
                }
            }
        }
    })
}


private const val durationMillis = 300

fun View.expand(animationListener: Animation.AnimationListener? = null) {
    measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val targetHeight = measuredHeight

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    layoutParams.height = 1
    visibility = View.VISIBLE
    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            layoutParams.height = if (interpolatedTime == 1f)
                ViewGroup.LayoutParams.WRAP_CONTENT
            else
                (targetHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = durationMillis.toLong()
    animationListener?.let { a.setAnimationListener(it) }
    startAnimation(a)
}

fun View.collapse(animationListener: Animation.AnimationListener? = null) {
    val initialHeight = measuredHeight

    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                visibility = View.GONE
            } else {
                layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = durationMillis.toLong()
    animationListener?.let { a.setAnimationListener(it) }
    startAnimation(a)
}