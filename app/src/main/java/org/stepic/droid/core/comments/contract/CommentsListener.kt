package org.stepic.droid.core.comments.contract

interface CommentsListener {

    fun onCommentsLoaded()

    fun onCommentsConnectionProblem()
}