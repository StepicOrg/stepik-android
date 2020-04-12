package org.stepic.droid.core

import androidx.annotation.MainThread
import org.stepic.droid.di.adaptive.AdaptiveCourseComponent
import org.stepic.droid.di.mainscreen.MainScreenComponent
import org.stepic.droid.di.splash.SplashComponent
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.injection.course.CourseComponent
import org.stepik.android.view.injection.profile.ProfileComponent
import org.stepik.android.view.injection.step.StepComponent

// TODO: 16.03.17 make more generic solution, for every component handling
interface ComponentManager {
    fun mainFeedComponent(): MainScreenComponent

    fun releaseMainFeedComponent()

    @MainThread
    fun splashComponent(): SplashComponent

    @MainThread
    fun releaseSplashComponent()

    @MainThread
    fun adaptiveCourseComponent(courseId: Long): AdaptiveCourseComponent

    @MainThread
    fun releaseAdaptiveCourseComponent(courseId: Long)


    @MainThread
    fun courseComponent(courseId: Long): CourseComponent

    @MainThread
    fun releaseCourseComponent(courseId: Long)

    @MainThread
    fun profileComponent(userId: Long): ProfileComponent

    /**
     * Steps
     */
    @MainThread
    fun stepParentComponent(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): StepComponent

    @MainThread
    fun stepComponent(stepId: Long): StepComponent
}
