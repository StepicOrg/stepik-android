package org.stepik.android.view.step_content_video.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_step_content_video.*
import kotlinx.android.synthetic.main.view_course_info_video.*
import kotlinx.android.synthetic.main.view_length_video_thumbnail.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.argument
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.presentation.step_content_video.VideoStepContentPresenter
import org.stepik.android.presentation.step_content_video.VideoStepContentView
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import javax.inject.Inject

class VideoStepContentFragment : Fragment(), VideoStepContentView {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            VideoStepContentFragment()
                .apply {
                    this.lessonData = lessonData
                    this.stepWrapper = stepPersistentWrapper
                }
    }

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: VideoStepContentPresenter

    private var lessonData: LessonData by argument()
    private var stepWrapper: StepPersistentWrapper by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        presenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(VideoStepContentPresenter::class.java)

        if (savedInstanceState == null) {
            presenter.fetchVideoLength(stepWrapper)
        }
    }

    private fun injectComponent() {
        App.component()
            .videoStepContentComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_content_video, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val thumbnail = stepWrapper.cachedVideo?.thumbnail
            ?: stepWrapper.step.block?.video?.thumbnail

        Glide.with(this)
            .load(thumbnail)
            .placeholder(R.drawable.general_placeholder)
            .into(videoThumbnail)

        videoContainer.setOnClickListener { openVideoPlayer() }
    }

    private fun openVideoPlayer() {
        if (stepWrapper.cachedVideo == null && stepWrapper.step.block?.video == null) {
            Snackbar
                .make(videoStepContent, R.string.step_content_video_no_video, Snackbar.LENGTH_SHORT)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
        } else {
            val thumbnail = stepWrapper.cachedVideo?.thumbnail
                ?: stepWrapper.step.block?.video?.thumbnail
            screenManager.showVideo(activity, VideoPlayerMediaData(
                thumbnail = thumbnail,
                title = lessonData.lesson.title ?: "",
                cachedVideo = stepWrapper.cachedVideo,
                externalVideo = stepWrapper.step.block?.video
            ))
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: VideoStepContentView.State) {
        val videoLengthText = (state as? VideoStepContentView.State.Loaded)
            ?.videoLength

        videoLength.changeVisibility(needShow = videoLengthText != null)
        videoLength.text = videoLengthText
    }
}