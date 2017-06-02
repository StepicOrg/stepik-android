package org.stepic.droid.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_video_step.*
import kotlinx.android.synthetic.main.player_placeholder.*
import kotlinx.android.synthetic.main.view_length_video_thumbnail.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.StepBaseFragment
import org.stepic.droid.core.presenters.StepQualityPresenter
import org.stepic.droid.core.presenters.VideoLengthPresenter
import org.stepic.droid.core.presenters.VideoStepPresenter
import org.stepic.droid.core.presenters.contracts.StepQualityView
import org.stepic.droid.core.presenters.contracts.VideoLengthView
import org.stepic.droid.core.presenters.contracts.VideoStepView
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent
import org.stepic.droid.events.steps.StepWasUpdatedEvent
import org.stepic.droid.model.Video
import org.stepic.droid.util.ThumbnailParser
import javax.inject.Inject

class VideoStepFragment : StepBaseFragment(),
        StepQualityView,
        VideoStepView,
        VideoLengthView {

    private var tempVideoQuality: String? = null

    @Inject
    lateinit var videoStepPresenter: VideoStepPresenter

    @Inject
    lateinit var stepQualityPresenter: StepQualityPresenter

    @Inject
    lateinit var videoLengthPresenter: VideoLengthPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun injectComponent() {
        App
                .getComponentManager()
                .routingComponent()
                .stepComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_video_step, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerWvEnhanced.visibility = View.GONE

        stepQualityPresenter.attachView(this)
        videoStepPresenter.attachView(this)
        videoLengthPresenter.attachView(this)

        videoStepPresenter.initVideo(step)

        playerLayout.setOnClickListener {
            playerLayout?.isClickable = false
            videoStepPresenter.playVideo(step)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.video_step_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        val qualityItemMenu = menu.findItem(R.id.action_quality)
        if (tempVideoQuality != null) {
            qualityItemMenu.isVisible = true
            qualityItemMenu.title = tempVideoQuality
        } else {
            qualityItemMenu.isVisible = false
        }
    }

    override fun onDestroyView() {
        videoLengthPresenter.detachView(this)
        videoStepPresenter.detachView(this)
        stepQualityPresenter.detachView(this)
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
                .placeholder(R.drawable.videoPlaceholderDrawable)
                .into(this.playerThumbnail)
    }

    private fun showTime(timeString: String?) {
        //at this callback view can be dead!
        timeString?.let {
            videoLengthTextView?.visibility = View.VISIBLE
            videoLengthTextView?.text = it
        }
    }

    override fun showQuality(qualityForView: String) {
        updateQualityMenu(qualityForView)
    }

    private fun updateQualityMenu(quality: String) {
        tempVideoQuality = quality
        activity.supportInvalidateOptionsMenu()
    }

    override fun onNeedOpenVideo(pathToVideo: String, videoId: Long) {
        playerLayout.isClickable = true
        screenManager.showVideo(activity, pathToVideo, videoId)
    }

    override fun onVideoLoaded(thumbnailPath: String?, video: Video) {
        //show thumbnail and show length should be synchronized event, because we do not show thumbnail now, only after fetching length
        stepQualityPresenter.determineQuality(video)
        videoLengthPresenter.fetchLength(video, step, thumbnailPath)
    }

    @Subscribe
    override fun onNewCommentWasAdded(event: NewCommentWasAddedOrUpdateEvent) {
        super.onNewCommentWasAdded(event)
    }

    @Subscribe
    override fun onStepWasUpdated(event: StepWasUpdatedEvent) {
        super.onStepWasUpdated(event)
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