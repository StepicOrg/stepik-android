package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepik.android.model.Video
import org.stepik.android.model.VideoUrl
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.greaterThanMaxQuality
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDialogInPlayer : VideoQualityDialogBase() {

    interface Callback {
        fun onQualityChanged(newUrlQuality: VideoUrl?)
    }

    companion object {
        private const val externalVideoKey = "externalVideoKey"
        private const val cachedVideoKey = "cachedVideoKey"
        private const val nowPlayingKey = "nowPlaying"


        fun newInstance(externalVideo: Video?, cachedVideo: Video?, nowPlayingUrl: String): VideoQualityDialogInPlayer {
            val fragment = VideoQualityDialogInPlayer()
            val bundle = Bundle()
            bundle.putParcelable(externalVideoKey, externalVideo)
            bundle.putParcelable(cachedVideoKey, cachedVideo)
            bundle.putString(nowPlayingKey, nowPlayingUrl)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun injectDependencies() {
        App.component().inject(this)
    }

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        init()

        val externalVideo: Video? = arguments.getParcelable(externalVideoKey)
        val cachedVideo: Video? = arguments.getParcelable(cachedVideoKey)
        val nowPlayingUrl = arguments.getString(nowPlayingKey)

        val listOfVideoUrl: MutableList<VideoUrl> =
                externalVideo
                        ?.urls
                        ?.filter {
                            !it.greaterThanMaxQuality()
                        }
                        ?.toMutableList()
                        ?: ArrayList()


        // if it is not external, than position will be after all external qualities
        val listOfPresentedQuality: MutableList<String> =
                externalVideo
                        ?.urls
                        ?.filter {
                            !it.greaterThanMaxQuality()
                        }
                        ?.mapNotNull { it.quality }
                        ?.toMutableList()
                        ?: ArrayList()

        cachedVideo?.urls?.firstOrNull()?.let {
            listOfPresentedQuality.add(getString(R.string.video_player_downloaded_quality, it.quality))
            listOfVideoUrl.add(it)
        }


        val position: Int = listOfVideoUrl
                .map { it.url }
                .indexOf(nowPlayingUrl)

        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.video_quality_playing)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    analytic.reportEvent(Analytic.Video.CANCEL_VIDEO_QUALITY)
                }
                .setSingleChoiceItems(listOfPresentedQuality.toTypedArray(), position) { dialog, which ->
                    val urlQuality = listOfVideoUrl[which]
                    (targetFragment as Callback).onQualityChanged(newUrlQuality = urlQuality)
                    dialog.dismiss()

                    val qualityForPlaying = listOfPresentedQuality[which]
                    threadPoolExecutor.execute {
                        val toSave = findNearest(qualityForPlaying, qualityToPositionMap.keys)
                        if (toSave != null) {
                            userPreferences.saveVideoQualityForPlaying(toSave)
                        }
                    }
                }

        return builder.create()
    }

    private fun findNearest(qualityForPlaying: String, availableQualities: Iterable<String>): String? {
        try {
            val weWant = Integer.parseInt(qualityForPlaying)
            var result: String? = null
            var bestDelta: Int = Int.MAX_VALUE
            availableQualities.forEach {
                val current = Integer.parseInt(it)
                val currentDelta = Math.abs(current - weWant)
                if (currentDelta < bestDelta) {
                    result = it
                    bestDelta = currentDelta
                }
                if (currentDelta == 0) {
                    return result
                }
            }
            return result
        } catch (exception: NumberFormatException) {
            //qualityForPlaying can be "Downloaded(360)"
            return null
        } catch (exception: Exception) {
            //when it can happen?
            analytic.reportError(Analytic.Error.CANT_PARSE_QUALITY, exception)
            return null
        }
    }
}