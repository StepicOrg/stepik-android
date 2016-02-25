package org.stepic.droid.events.video

import org.stepic.droid.model.CachedVideo
import org.stepic.droid.model.Lesson

data class VideoCachedOnDiskEvent(val stepId : Long, val lesson: Lesson, val video : CachedVideo)
