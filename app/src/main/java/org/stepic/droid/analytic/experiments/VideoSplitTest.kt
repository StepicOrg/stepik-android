package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class VideoSplitTest
@Inject
constructor(
    analytics: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<VideoSplitTest.Group>(
    analytics,
    sharedPreferenceHelper,

    name = "video_in_course_description",
    groups = Group.values()
) {
    enum class Group(
        val isVideoEnabled: Boolean,
        override val distribution: Int
    ) : SplitTest.Group {
        Control(isVideoEnabled = true, distribution = 3),
        NoVideo(isVideoEnabled = false, distribution = 1)
    }
}