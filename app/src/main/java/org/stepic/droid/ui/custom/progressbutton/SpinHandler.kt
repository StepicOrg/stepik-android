package org.stepic.droid.ui.custom.progressbutton

import android.os.Handler
import android.os.Message
import java.lang.ref.WeakReference

class SpinHandler(progressWheel: ProgressWheel) : Handler() {
    val progressWheelReference: WeakReference<ProgressWheel>

    init {
        progressWheelReference = WeakReference(progressWheel)
    }


    override fun handleMessage(msg: Message?) {
        progressWheelReference.get()?.let { progressWheel ->
            with(progressWheel) {
                invalidate()
                if (isSpinning) {
                    progress += spinSpeed
                    if (progress > 360) {
                        progress = 0
                    }
                }
                sendEmptyMessageDelayed(0, delayMillis.toLong())
            }
        }
    }
}
