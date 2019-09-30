package org.stepic.droid.core

import android.support.annotation.MainThread
import org.stepic.droid.di.adaptive.AdaptiveCourseComponent
import org.stepic.droid.di.course_general.CourseGeneralComponent
import org.stepic.droid.di.login.LoginComponent
import org.stepic.droid.di.mainscreen.MainScreenComponent
import org.stepic.droid.di.splash.SplashComponent
import org.stepik.android.view.injection.course.CourseComponent

// TODO: 16.03.17 make more generic solution, for every component handling
interface ComponentManager {
    fun mainFeedComponent(): MainScreenComponent

    fun releaseMainFeedComponent()

    fun loginComponent(tag: String): LoginComponent

    fun releaseLoginComponent(tag: String)

    @MainThread
    fun splashComponent(): SplashComponent

    @MainThread
    fun releaseSplashComponent()

    @MainThread
    fun courseGeneralComponent(): CourseGeneralComponent

    @MainThread
    fun adaptiveCourseComponent(courseId: Long): AdaptiveCourseComponent

    @MainThread
    fun releaseAdaptiveCourseComponent(courseId: Long)


    @MainThread
    fun courseComponent(courseId: Long): CourseComponent

    @MainThread
    fun releaseCourseComponent(courseId: Long)
}
