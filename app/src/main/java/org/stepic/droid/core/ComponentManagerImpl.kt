package org.stepic.droid.core

import org.stepic.droid.di.AppCoreComponent
import org.stepic.droid.di.adaptive.AdaptiveCourseComponent
import org.stepic.droid.di.course_general.CourseGeneralComponent
import org.stepic.droid.di.mainscreen.MainScreenComponent
import org.stepic.droid.di.splash.SplashComponent
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.SuppressFBWarnings
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.injection.course.CourseComponent
import org.stepik.android.view.injection.profile.ProfileComponent
import org.stepik.android.view.injection.step.StepComponent
import timber.log.Timber
import java.lang.ref.WeakReference

class ComponentManagerImpl(private val appCoreComponent: AppCoreComponent) : ComponentManager {

    // Splash

    private var _splashComponent: SplashComponent? = null

    override fun splashComponent(): SplashComponent {
        if (_splashComponent == null) {
            _splashComponent = appCoreComponent.splashComponent().build()
        }
        return _splashComponent!!
    }

    override fun releaseSplashComponent() {
        _splashComponent = null
    }

    // Adaptive courses

    private val adaptiveCourseComponentMap = HashMap<Long, AdaptiveCourseComponent>()
    private val adaptiveCourseComponentCountMap = HashMap<Long, Int>()

    override fun adaptiveCourseComponent(courseId: Long): AdaptiveCourseComponent {
        val count = adaptiveCourseComponentCountMap[courseId] ?: 0
        adaptiveCourseComponentCountMap[courseId] = count + 1
        return adaptiveCourseComponentMap.getOrPut(courseId) {
            appCoreComponent
                    .adaptiveCourseComponentBuilder()
                    .courseId(courseId)
                    .build()
        }
    }

    @SuppressFBWarnings(value = ["RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"], justification = "false positive")
    override fun releaseAdaptiveCourseComponent(courseId: Long) {
        val count: Int = adaptiveCourseComponentCountMap[courseId] ?: throw IllegalStateException("release adaptive course component, which is not allocated")
        if (count == 1) {
            //it is last
            adaptiveCourseComponentMap.remove(courseId)
            adaptiveCourseComponentCountMap.remove(courseId)
        } else {
            adaptiveCourseComponentCountMap[courseId] = count - 1
        }
    }

    // Course

    private val _courseComponentMap = hashMapOf<Long, ComponentHolder<CourseComponent>>()

    override fun courseComponent(courseId: Long): CourseComponent =
            _courseComponentMap.getOrPut(courseId, ::ComponentHolder).get {
                appCoreComponent.courseComponentBuilder().courseId(courseId).build()
            }

    override fun releaseCourseComponent(courseId: Long) =
            _courseComponentMap[courseId]
                    ?.release()
                    ?: throw IllegalStateException("release course = $courseId component, which is not allocated")

    // Profile

    private val _profileComponentMap = hashMapOf<Long, WeakComponentHolder<ProfileComponent>>()

    override fun profileComponent(userId: Long): ProfileComponent =
        _profileComponentMap.getOrPut(userId, ::WeakComponentHolder).get {
            appCoreComponent.profileComponentBuilder().userId(userId).build()
        }

    /**
     * Steps
     */
    private val _stepComponentMap = hashMapOf<Long, WeakComponentHolder<StepComponent>>()

    override fun stepParentComponent(
        stepPersistentWrapper: StepPersistentWrapper,
        lessonData: LessonData
    ): StepComponent =
        _stepComponentMap.getOrPut(stepPersistentWrapper.step.id, ::WeakComponentHolder).get {
            appCoreComponent
                .stepComponentBuilder()
                .stepWrapper(stepPersistentWrapper)
                .lessonData(lessonData)
                .build()
        }

    override fun stepComponent(stepId: Long): StepComponent =
        _stepComponentMap[stepId]?.get() ?: throw IllegalStateException("StepComponent with id=$stepId not initialized")

    // Main Screen

    private var mainScreenComponentProp: MainScreenComponent? = null

    override fun mainFeedComponent(): MainScreenComponent {
        synchronized(this) {
            if (mainScreenComponentProp == null) {
                mainScreenComponentProp = appCoreComponent
                        .mainScreenComponentBuilder()
                        .build()
            }
            return mainScreenComponentProp!!
        }
    }

    override fun releaseMainFeedComponent() {
        synchronized(this) {
            mainScreenComponentProp = null
        }
    }

    // Course general

    private val _courseGeneralComponent by lazy {
        appCoreComponent
                .courseGeneralComponentBuilder()
                .build()
    }

    override fun courseGeneralComponent(): CourseGeneralComponent = _courseGeneralComponent
}

class ComponentHolder<T> {
    private var refCount = 0
    private var component: T? = null

    fun get(creationBlock: () -> T): T {
        if (component == null) {
            component = creationBlock.invoke()
        }

        refCount++
        Timber.d("$component allocated with refCount = $refCount")
        return component!!
    }

    fun release() {
        refCount--
        if (refCount == 0) {
            component = null
        }

        Timber.d("$component released with new refCount = $refCount")

        if (refCount < 0) {
            throw IllegalStateException("released component greater than got")
        }
    }

}

class WeakComponentHolder<T> {
    private var componentReference: WeakReference<T> = WeakReference<T>(null)

    fun get(creationBlock: () -> T): T {
        val component = componentReference.get() ?: creationBlock()
        componentReference = WeakReference(component)

        return component
    }

    fun get(): T? =
        componentReference.get()
}
