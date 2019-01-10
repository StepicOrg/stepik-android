package org.stepic.droid.ui.dialogs

import android.support.v4.app.DialogFragment
import java.util.*

abstract class VideoQualityDialogBase : DialogFragment() {
    protected val qualityToPositionMap: MutableMap<String, Int> = HashMap()
    protected val positionToQualityMap: MutableMap<Int, String> = HashMap()

    protected fun init() {
        injectDependencies()
        if (qualityToPositionMap.isEmpty() || positionToQualityMap.isEmpty()) {
            initMaps()
        }
    }

    abstract fun injectDependencies()

    private fun initMaps() {
        qualityToPositionMap.put("270", 0)
        qualityToPositionMap.put("360", 1)
        qualityToPositionMap.put("720", 2)

        positionToQualityMap.put(0, "270")
        positionToQualityMap.put(1, "360")
        positionToQualityMap.put(2, "720")
    }
}