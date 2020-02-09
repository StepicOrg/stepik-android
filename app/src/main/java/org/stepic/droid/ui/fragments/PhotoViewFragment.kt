package org.stepic.droid.ui.fragments

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoViewAttacher
import kotlinx.android.synthetic.main.fragment_photo_view.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import ru.nobird.android.view.base.ui.extension.argument
import kotlin.math.abs
import kotlin.math.min

class PhotoViewFragment : FragmentBase() {

    companion object {
        fun newInstance(path: String): PhotoViewFragment =
            PhotoViewFragment()
                .apply {
                    this.url = path
                }
    }

    private lateinit var photoViewAttacher: PhotoViewAttacher
    private var screenHeight: Int = 0
    private var dismissPathLength: Int = 0

    private var url: String by argument()

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
        retainInstance = true
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_photo_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        photoViewAttacher = PhotoViewAttacher(zoomableImageView!!)
        screenHeight = Resources.getSystem().displayMetrics.heightPixels
        dismissPathLength = resources.getDimensionPixelSize(R.dimen.dismiss_path_length)

        verticalDragLayout.setOnDragListener { dy ->
            if (photoViewAttacher.scale > 1f) return@setOnDragListener
            val alpha = 1 - min(abs(dy / (3 * dismissPathLength)), 1f)
            backgroundColorView.alpha = alpha
            toolbar.alpha = alpha
            zoomableImageView!!.translationY = -dy
        }

        verticalDragLayout.setOnReleaseDragListener { dy ->
            if (photoViewAttacher.scale > 1f) return@setOnReleaseDragListener
                    if (abs(dy) > dismissPathLength) {
                        zoomableImageView.isVisible = false
                        (activity as? AppCompatActivity)?.finish()
                    } else {
                        backgroundColorView.alpha = 1f
                        toolbar.alpha = 1f
                        zoomableImageView.translationY = 0f
                    }
            Unit
        }

        retryButton.setOnClickListener {
            internetProblemRootView.isVisible = false
            loadImage()
        }
        loadImage()
    }

    private fun loadImage() {
        Glide.with(requireContext())
            .asBitmap()
            .load(url)
            .fitCenter()
            .into<Target<Bitmap>>(target)
    }

    private fun setUpToolbar() {
        val appCompatActivity = activity as? AppCompatActivity
        appCompatActivity?.setSupportActionBar(toolbar)
        val supportActionBar = appCompatActivity?.supportActionBar

        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false)
            supportActionBar.setDisplayShowHomeEnabled(true)
            supportActionBar.setHomeButtonEnabled(true)
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
        }
    }
}
