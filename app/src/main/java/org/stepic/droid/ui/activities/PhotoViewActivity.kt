package org.stepic.droid.ui.activities

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoViewAttacher
import kotlinx.android.synthetic.main.fragment_photo_view.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import kotlin.math.abs
import kotlin.math.min

class PhotoViewActivity : FragmentActivityBase() {
    companion object {
        const val EXTRA_PATH = "extra_path"
    }

    private lateinit var photoViewAttacher: PhotoViewAttacher
    private var screenHeight: Int = 0
    private var dismissPathLength: Int = 0

    private val target = object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            internetProblemRootView.isVisible = false
            zoomableImageView.setImageBitmap(resource)
            photoViewAttacher.update()
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            internetProblemRootView.isVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_photo_view)
        setUpToolbar()
        photoViewAttacher = PhotoViewAttacher(zoomableImageView)
        screenHeight = Resources.getSystem().displayMetrics.heightPixels
        dismissPathLength = resources.getDimensionPixelSize(R.dimen.dismiss_path_length)

        verticalDragLayout.setOnDragListener { dy ->
            if (photoViewAttacher.scale > 1f) return@setOnDragListener
            val alpha = 1 - min(abs(dy / (3 * dismissPathLength)), 1f)
            backgroundColorView.alpha = alpha
            toolbar.alpha = alpha
            zoomableImageView.translationY = -dy
        }

        verticalDragLayout.setOnReleaseDragListener { dy ->
            if (photoViewAttacher.scale > 1f) return@setOnReleaseDragListener
            if (abs(dy) > dismissPathLength) {
                zoomableImageView.isVisible = false
                finish()
            } else {
                backgroundColorView.alpha = 1f
                toolbar.alpha = 1f
                zoomableImageView.translationY = 0f
            }
        }

        val url = intent.getStringExtra(EXTRA_PATH)

        retryButton.setOnClickListener {
            internetProblemRootView.isVisible = false
            loadImage(url)
        }
        loadImage(url)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.no_transition)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    private fun loadImage(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .fitCenter()
            .into<Target<Bitmap>>(target)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar

        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false)
            supportActionBar.setDisplayShowHomeEnabled(true)
            supportActionBar.setHomeButtonEnabled(true)
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
        }
    }
}
