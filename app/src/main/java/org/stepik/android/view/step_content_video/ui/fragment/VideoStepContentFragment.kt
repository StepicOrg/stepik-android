package org.stepik.android.view.step_content_video.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_step_content_video.*
import kotlinx.android.synthetic.main.view_course_info_video.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.argument
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import javax.inject.Inject

class VideoStepContentFragment : Fragment() {
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

    private var lessonData: LessonData by argument()
    private var stepWrapper: StepPersistentWrapper by argument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
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

        videoContainer.setOnClickListener {
            screenManager.showVideo(activity, VideoPlayerMediaData(
                thumbnail = thumbnail,
                title = lessonData.lesson.title ?: "",
                cachedVideo = stepWrapper.cachedVideo,
                externalVideo = stepWrapper.step.block?.video
            ))
        }
    }
}