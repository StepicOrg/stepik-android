package org.stepik.android.presentation.progress

import org.stepik.android.model.Progress

interface ProgressFeature {
    sealed class Message {
        data class ProgressUpdate(val progress: Progress) : Message()
    }
    sealed class Action
}