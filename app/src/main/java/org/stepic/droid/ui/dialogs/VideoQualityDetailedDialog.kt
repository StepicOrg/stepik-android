package org.stepic.droid.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class VideoQualityDetailedDialog : VideoQualityDialogBase() {
    companion object {
        const val TAG = "VideoQualityDetailedDialog"
        const val VIDEO_QUALITY_REQUEST_CODE = 9048

        const val UNIT_KEY = "unit"
        const val SECTION_KEY = "section"
        const val COURSE_KEY = "course"

        const val VIDEO_QUALITY = "video_quality"

        fun newInstance(course: Course? = null, section: Section? = null, unit: Unit? = null): VideoQualityDetailedDialog {
            val dialog = VideoQualityDetailedDialog()
            dialog.arguments = Bundle(1)
                .apply {
                    course?.let { putParcelable(COURSE_KEY, it) }
                    section?.let { putParcelable(SECTION_KEY, it) }
                    unit?.let { putParcelable(UNIT_KEY, it) }
                }
            return dialog
        }
    }

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var threadPoolExecutor: ThreadPoolExecutor

    @Inject
    lateinit var mainHandler: MainHandler

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        init()
        val layoutInflater = LayoutInflater.from(context)
        val explanationView = layoutInflater.inflate(R.layout.dialog_video_quality_detailed, null)
        val checkbox = explanationView.findViewById<CheckBox>(R.id.video_quality_do_not_ask_checkbox)
        val selectionItems = explanationView.findViewById<ListView>(R.id.video_quality_list_choices)

        var chosenOptionPosition = qualityToPositionMap[userPreferences.qualityVideo]!!

        selectionItems.adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_single_choice, resources.getStringArray(R.array.video_quality))
        selectionItems.choiceMode = ListView.CHOICE_MODE_SINGLE
        selectionItems.setItemChecked(chosenOptionPosition, true)
        selectionItems.setOnItemClickListener { _, _, position, _ ->
            chosenOptionPosition = position
        }

        val builder = AlertDialog.Builder(requireContext())
        builder
                .setTitle(R.string.video_quality)
                .setView(explanationView)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    analytic.reportEvent(Analytic.Interaction.CANCEL_VIDEO_QUALITY_DETAILED)
                }
                .setPositiveButton(R.string.ok) { _, _ ->
                    val qualityString = positionToQualityMap[chosenOptionPosition]
                    analytic.reportEventWithIdName(Analytic.Preferences.VIDEO_QUALITY, chosenOptionPosition.toString(), qualityString)

                    threadPoolExecutor.execute {
                        userPreferences.storeQualityVideo(qualityString)
                        mainHandler.post {
                            val args = arguments
                                ?: return@post

                            targetFragment
                                ?.onActivityResult(
                                    VIDEO_QUALITY_REQUEST_CODE,
                                    Activity.RESULT_OK,
                                    Intent()
                                        .putExtras(args)
                                        .putExtra(VIDEO_QUALITY, qualityString)
                                )
                        }
                    }

                    val isNeedExplanation = !checkbox.isChecked
                    threadPoolExecutor.execute {
                        sharedPreferencesHelper.isNeedToShowVideoQualityExplanation = isNeedExplanation
                    }
                }


        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Timber.d("onDismiss")

    }

    override fun injectDependencies() {
        App.component().inject(this)
    }
}
