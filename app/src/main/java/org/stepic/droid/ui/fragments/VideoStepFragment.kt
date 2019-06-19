package org.stepic.droid.ui.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.fragment_video_step.*
import kotlinx.android.synthetic.main.player_placeholder.*
import kotlinx.android.synthetic.main.view_length_video_thumbnail.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.StepBaseFragment
import org.stepic.droid.core.presenters.VideoLengthPresenter
import org.stepic.droid.core.presenters.contracts.VideoLengthView
import org.stepic.droid.util.ThumbnailParser
import javax.inject.Inject

class VideoStepFragment : StepBaseFragment(),
        VideoLengthView {

    @Inject
    lateinit var videoLengthPresenter: VideoLengthPresenter

    override fun injectComponent() {
        App
                .componentManager()
                .stepComponent(step.id)
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_video_step, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoLengthPresenter.attachView(this)


        playerLayout.setOnClickListener {
            playerLayout?.isClickable = false
        }
    }

    override fun attachStepTextWrapper() {} // no need in step text wrapper
    override fun detachStepTextWrapper() {}

    override fun onDestroyView() {
        videoLengthPresenter.detachView(this)
        playerLayout.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun setThumbnail(thumbnail: String?, timeString: String?) {
        if (thumbnail == null) {
            return
        }
        val uri = ThumbnailParser.getUriForThumbnail(thumbnail)
        Glide
            .with(requireContext())
            .load(uri)
            .placeholder(R.drawable.video_placeholder_drawable)
            .addListener(object : RequestListener<Drawable>{
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    //at this callback view can be dead!
                    showTime(timeString)
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    //at this callback view can be dead!
                    showTime(timeString)
                    return false
                }

            })
            .into(this.playerThumbnail)
    }

    private fun showTime(timeString: String?) {
        //at this callback view can be dead!
        timeString?.let {
            videoLength?.visibility = View.VISIBLE
            videoLength?.text = it
        }
    }

    override fun onVideoLengthDetermined(presentationString: String, thumbnail: String?) {
        setThumbnail(thumbnail, presentationString)
    }

    override fun onVideoLengthFailed(thumbnail: String?) {
        setThumbnail(thumbnail, null)
    }
}