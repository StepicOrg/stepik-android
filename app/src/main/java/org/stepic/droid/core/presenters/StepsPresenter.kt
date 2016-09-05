package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.StepsView
import org.stepic.droid.model.Lesson

class StepsPresenter : PresenterBase<StepsView>() {
    var isInited = false

    override fun attachView(view: StepsView) {
        super.attachView(view)
    }

    fun showSteps(lesson: Lesson) {
        //todo get from cache -> show if not empty -> update from api
    }

}
