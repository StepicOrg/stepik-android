package org.stepik.android.presentation.profile_courses

import org.stepic.droid.core.presenters.PresenterContract
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.PresenterBase
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface ProfileCoursesView : CourseShowable, StateView<ProfileCoursesView.State> {
    sealed class State {
        object Idle : State()
        object SilentLoading : State()
        object Empty : State()
        object Error : State()

        class Content(val courses: List<Course>) : State()
    }

    fun setBlockingLoading(isLoading: Boolean)

    fun showSteps(course: Course, lastStep: LastStep)
}

interface CourseShowable {
    fun showCourse(course: Course, isAdaptive: Boolean)
}

interface ContinueCourseAction {
    fun continueCourse(course: Course)
}

class ContinueCourseActionImpl
@Inject
constructor(
    private val viewContainer: ViewContainer<out CourseShowable>
) : ContinueCourseAction {
    init {
        Timber.d(viewContainer.toString())
    }

    override fun continueCourse(course: Course) {
        viewContainer.view?.showCourse(course, isAdaptive = false)
    }
}

interface StateContainer<T> : ReadWriteProperty<Any, T> {
    var state: T

    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        state

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        state = value
    }
}

interface StateView<T> {
    fun setState(state: T)
}

interface ViewContainer<V> {
    var view: V?
}

class DefaultStateContainer<T, V : StateView<T>>(
    initialState: T
) : PresenterContract<V>, StateContainer<T>, ViewContainer<V> {
    override var view: V? = null

    override var state: T = initialState
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: V) {
        this.view = view
        view.setState(state)
    }

    override fun detachView(view: V) {
        this.view = null
    }
}

