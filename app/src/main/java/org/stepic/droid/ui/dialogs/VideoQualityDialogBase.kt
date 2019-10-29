package org.stepic.droid.ui.dialogs

import androidx.fragment.app.DialogFragment
import java.util.HashMap

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
        qualityToPositionMap["270"] = 0
        qualityToPositionMap["360"] = 1
        qualityToPositionMap["720"] = 2
        qualityToPositionMap["1080"] = 3

        positionToQualityMap[0] = "270"
        positionToQualityMap[1] = "360"
        positionToQualityMap[2] = "720"
        positionToQualityMap[3] = "1080"
    }
}