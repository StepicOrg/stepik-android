package org.stepik.android.view.lesson.ui.interfaces

import org.stepik.android.domain.step.model.StepNavigationDirection

interface Moveable {

    /**
     *  if [isAutoplayEnabled] next item will be played
     * @return true, if handled by parent
     */
    fun move(isAutoplayEnabled: Boolean = false, stepNavigationDirection: StepNavigationDirection = StepNavigationDirection.NEXT): Boolean
}
