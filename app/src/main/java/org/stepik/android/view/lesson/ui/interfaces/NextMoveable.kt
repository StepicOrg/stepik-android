package org.stepik.android.view.lesson.ui.interfaces

interface NextMoveable {

    /**
     *  if [isAutoplayEnabled] next item will be played
     * @return true, if handled by parent
     */
    fun moveNext(isAutoplayEnabled: Boolean = false): Boolean
}
