package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.StepsView
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Unit

class StepsPresenter : PresenterBase<StepsView>() {

    var lesson: Lesson? = null
        private set

    var unit: Unit? = null


    fun init(lesson: Lesson?, unit: Unit?, simpleLessonId: Long, simpleUnitId: Long, defaultStepPosition: Long) {
        if (this.lesson != null) return



        //view?.onLessonPrepared
        //showSteps
    }

    private fun showSteps() {
        //todo get from cache -> show if not empty -> update from api -> show if you can (handle if steps updated)
    }

}
