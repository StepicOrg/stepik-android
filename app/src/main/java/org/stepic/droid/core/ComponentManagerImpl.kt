package org.stepic.droid.core

import org.stepic.droid.di.AppCoreComponent
import org.stepic.droid.di.course_general.CourseGeneralComponent
import org.stepic.droid.di.login.LoginComponent
import org.stepic.droid.di.mainscreen.MainScreenComponent
import org.stepic.droid.di.routing.RoutingComponent
import org.stepic.droid.di.step.StepComponent

class ComponentManagerImpl(private val appCoreComponent: AppCoreComponent) : ComponentManager {

//    Step

    private val stepComponentMap = HashMap<Long, StepComponent>()

    override fun stepComponent(stepId: Long) = stepComponentMap.getOrPut(stepId) {
        routingComponent()
                .stepComponentBuilder()
                .build()
    }

    override fun releaseStepComponent(stepId: Long) {
        releaseRoutingComponent()
        stepComponentMap.remove(stepId)
    }


//    Login

    private val loginComponentMap = HashMap<String, LoginComponent>()

    override fun releaseLoginComponent(tag: String) {
        loginComponentMap.remove(tag)
    }

    override fun loginComponent(tag: String) =
            loginComponentMap.getOrPut(tag) {
                appCoreComponent
                        .loginComponentBuilder()
                        .build()
            }

//    Main Screen

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

//    Routing

    private val routingComponentHolder = ComponentHolder<RoutingComponent>()

    override fun routingComponent(): RoutingComponent {
        return routingComponentHolder.get {
            appCoreComponent
                    .routingComponentBuilder()
                    .build()
        }
    }


    override fun releaseRoutingComponent() {
        routingComponentHolder.release()
    }


//    Course general

    private val courseGeneralComponentHolder = ComponentHolder<CourseGeneralComponent>()

    override fun courseGeneralComponent(): CourseGeneralComponent {
        return courseGeneralComponentHolder.get {
            appCoreComponent
                    .courseGeneralComponentBuilder()
                    .build()
        }
    }

    override fun releaseCourseGeneralComponent() {
        courseGeneralComponentHolder.release()
    }

}

class ComponentHolder<T> {
    private var refCount = 0
    private var component: T? = null

    fun get(creationBlock: () -> T): T {
        if (component == null) {
            component = creationBlock.invoke()
        }

        refCount++
        return component!!
    }

    fun release() {
        refCount--
        if (refCount == 0) {
            component = null
        }
        if (refCount < 0) {
            throw IllegalStateException("released component greater than got")
        }
    }

}
