package org.stepic.droid.ui.activities

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import dev.androidbroadcast.vbpd.viewBinding
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoViewAttacher
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.databinding.FragmentPhotoViewBinding
import org.stepik.android.view.glide.model.GlideRequestFactory
import kotlin.math.abs
import kotlin.math.min

class PhotoViewActivity : FragmentActivityBase() {
    companion object {
        const val EXTRA_PATH = "extra_path"
    }

    private val photoViewBinding: FragmentPhotoViewBinding by viewBinding(FragmentPhotoViewBinding::bind)
    private lateinit var photoViewAttacher: PhotoViewAttacher
    private var screenHeight: Int = 0
    private var dismissPathLength: Int = 0

    private val target = object : CustomTarget<PictureDrawable>() {
        override fun onResourceReady(resource: PictureDrawable, transition: Transition<in PictureDrawable>?) {
            photoViewBinding.internetProblemRootView.isVisible = false
            photoViewBinding.zoomableImageView.setImageDrawable(resource)
            photoViewAttacher.update()
//            Timber.d("resource ready")
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            photoViewBinding.internetProblemRootView.isVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_photo_view)
        setUpToolbar()
        photoViewAttacher = PhotoViewAttacher(photoViewBinding.zoomableImageView)
        screenHeight = Resources.getSystem().displayMetrics.heightPixels
        dismissPathLength = resources.getDimensionPixelSize(R.dimen.dismiss_path_length)

        photoViewBinding.verticalDragLayout.setOnDragListener { dy ->
            if (photoViewAttacher.scale > 1f) return@setOnDragListener
            val alpha = 1 - min(abs(dy / (3 * dismissPathLength)), 1f)
            photoViewBinding.backgroundColorView.alpha = alpha
            photoViewBinding.toolbar.alpha = alpha
            photoViewBinding.zoomableImageView.translationY = -dy
        }

        photoViewBinding.verticalDragLayout.setOnReleaseDragListener { dy ->
            if (photoViewAttacher.scale > 1f) return@setOnReleaseDragListener
            if (abs(dy) > dismissPathLength) {
                photoViewBinding.zoomableImageView.isVisible = false
                finish()
            } else {
                photoViewBinding.backgroundColorView.alpha = 1f
                photoViewBinding.toolbar.alpha = 1f
                photoViewBinding.zoomableImageView.translationY = 0f
            }
        }

        val url = requireNotNull(intent.getStringExtra(EXTRA_PATH))

        photoViewBinding.retryButton.setOnClickListener {
            photoViewBinding.internetProblemRootView.isVisible = false
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
        GlideRequestFactory
            .create(this, null)
            .load(url)
            .fitCenter()
            .into(target)
    }

    private fun setUpToolbar() {
        setSupportActionBar(photoViewBinding.toolbar)
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
