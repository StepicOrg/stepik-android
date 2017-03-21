package org.stepic.droid.core.presenters.contracts

interface VideoLengthView {
    fun onVideoLengthDetermined(presentationString: String, thumbnail : String?)

    fun onVideoLengthFailed(thumbnail: String?)
}
