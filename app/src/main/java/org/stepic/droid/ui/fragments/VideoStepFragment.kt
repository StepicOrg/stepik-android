package org.stepic.droid.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.fragment_video_step.*
import kotlinx.android.synthetic.main.player_placeholder.*
import kotlinx.android.synthetic.main.view_length_video_thumbnail.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.StepBaseFragment
import org.stepic.droid.core.presenters.VideoLengthPresenter
import org.stepic.droid.core.presenters.VideoStepPresenter
import org.stepic.droid.core.presenters.contracts.VideoLengthView
import org.stepic.droid.core.presenters.contracts.VideoStepView
import org.stepik.android.model.Video
import org.stepic.droid.util.ThumbnailParser
import javax.inject.Inject

class VideoStepFragment : StepBaseFragment(),
        VideoStepView,
        VideoLengthView {

    @Inject
    lateinit var videoStepPresenter: VideoStepPresenter

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

        videoStepPresenter.attachView(this)
        videoLengthPresenter.attachView(this)

        videoStepPresenter.initVideo(stepWrapper)

        playerLayout.setOnClickListener {
            playerLayout?.isClickable = false
            videoStepPresenter.playVideo(stepWrapper)
        }
    }

    override fun attachStepTextWrapper() {} // no need in step text wrapper
    override fun detachStepTextWrapper() {}

    override fun onDestroyView() {
        videoLengthPresenter.detachView(this)
        videoStepPresenter.detachView(this)
        playerLayout.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun setThumbnail(thumbnail: String?, timeString: String?) {
        if (thumbnail == null) {
            return
        }

        val uri = ThumbnailParser.getUriForThumbnail(thumbnail)
        Glide
                .with(context)
                .load(uri)
                .listener(object : RequestListener<Uri, GlideDrawable> {
                    override fun onException(e: Exception, model: Uri, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                        //at this callback view can be dead!
                        showTime(timeString)
                        return false
                    }

                    override fun onResourceReady(resource: GlideDrawable, model: Uri, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        //at this callback view can be dead!
                        showTime(timeString)
                        return false
                    }
                })
                .placeholder(R.drawable.video_placeholder_drawable)
                .into(this.playerThumbnail)
    }

    private fun showTime(timeString: String?) {
        //at this callback view can be dead!
        timeString?.let {
            videoLengthTextView?.visibility = View.VISIBLE
            videoLengthTextView?.text = it
        }
    }

    override fun onNeedOpenVideo(videoId: Long, cachedVideo: Video?, externalVideo: Video?) {
        playerLayout.isClickable = true
        screenManager.showVideo(activity, cachedVideo, externalVideo)
    }

    override fun onVideoLoaded(thumbnailPath: String?, cachedVideo: Video?, externalVideo: Video?) {
        //show thumbnail and show length should be synchronized event, because we do not show thumbnail now, only after fetching length
        val video = cachedVideo ?: externalVideo
        if (video != null) {
            videoLengthPresenter.fetchLength(video, thumbnailPath)
        } else {
            analytic.reportEvent(Analytic.Error.NO_VIDEO_ON_STEP_SHOWING)
        }
    }

    override fun onInternetProblem() {
        playerLayout.isClickable = true
        Toast.makeText(context, R.string.sync_problem, Toast.LENGTH_SHORT).show()
    }

    override fun onVideoLengthDetermined(presentationString: String, thumbnail: String?) {
        setThumbnail(thumbnail, presentationString)
    }

    override fun onVideoLengthFailed(thumbnail: String?) {
        setThumbnail(thumbnail, null)
    }
}