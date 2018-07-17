package org.stepic.droid.ui.activities

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Section
import org.stepik.android.model.structure.Unit
import org.stepic.droid.ui.fragments.LessonFragment
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.HtmlHelper

class StepsActivity : SingleFragmentActivity() {

    companion object {
        val needReverseAnimationKey = "needReverseAnimation"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val isNeedReverseAnimation = intent?.extras?.getBoolean(needReverseAnimationKey, false)
            if (isNeedReverseAnimation == true) {
                overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end)
            } else {
                overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start)
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun createFragment(): Fragment {
        val extras = intent.extras

        val action = intent?.action
        if (action == AppConstants.OPEN_NOTIFICATION) {
            analytic.reportEvent(Analytic.Notification.OPEN_NOTIFICATION)
        }

        val section = extras?.getParcelable<Section>(AppConstants.KEY_SECTION_BUNDLE)
        val unit: Unit? = extras?.getParcelable<Unit>(AppConstants.KEY_UNIT_BUNDLE) // UNit can be null
        val lesson = extras?.getParcelable<Lesson>(AppConstants.KEY_LESSON_BUNDLE) //Lesson can be null in intent and if url is broken
        val fromPrevious: Boolean = extras?.getBoolean(needReverseAnimationKey, false) ?: false

        val dataUri = intent?.data

        if (lesson == null && dataUri != null) {
            if (action != null && action != AppConstants.INTERNAL_STEPIK_ACTION) {
                analytic.reportEvent(Analytic.DeepLink.USER_OPEN_STEPS_LINK);
                analytic.reportEvent(Analytic.DeepLink.USER_OPEN_LINK_GENERAL);
                if (sharedPreferenceHelper.authResponseFromStore == null) {
                    analytic.reportEvent(Analytic.DeepLink.ANONYMOUS_OPEN_STEPS_LINK)
                }
            }
            //All can be -1
            val simpleLessonId: Long = getSimpleLessonId(dataUri)
            val simpleStepPosition: Long = getStepPosition(dataUri)
            val simpleUnitId: Long = getUnitSimpleId(dataUri)
            val discussionSampleId = getDiscussionSampleId(dataUri)
            return LessonFragment.newInstance(simpleUnitId, simpleLessonId, simpleStepPosition, discussionSampleId)

        } else {
            return LessonFragment.newInstance(unit, lesson, fromPrevious, section)
        }
    }

    private fun getDiscussionSampleId(dataUri: Uri): Long {
        val rawQuery = dataUri.getQueryParameter("discussion");
        return parseLong(rawQuery)
    }

    private fun getUnitSimpleId(dataUri: Uri): Long {
        val rawQuery = dataUri.getQueryParameter("unit");
        return parseLong(rawQuery)
    }

    private fun getStepPosition(dataUri: Uri): Long {
        var stepNumberRaw: String? = null
        //0 -> "lesson"
        //1 -> lesson-name-id or FAKE
        //2 -> "step"
        //3 -> stepId
        val pathSegments = dataUri.pathSegments
        if (pathSegments != null && pathSegments.size >= 4) {
            stepNumberRaw = pathSegments[3]
        }
        return parseLong(stepNumberRaw)
    }

    private fun parseLong(raw: String?) =
            if (raw == null) {
                -1L;
            } else {
                try {
                    java.lang.Long.parseLong(raw)
                } catch (numberFormatException: NumberFormatException) {
                    -1L
                }
            }

    private fun getSimpleLessonId(dataUri: Uri): Long {
        var lessonSlug: String? = null
        val pathSegments = dataUri.pathSegments
        if (pathSegments != null && pathSegments.size >= 2) {
            lessonSlug = pathSegments[1]
        }

        var lessonId: Long? = null
        if (lessonSlug != null) {
            lessonId = HtmlHelper.parseIdFromSlug(lessonSlug)
        }

        return lessonId ?: -1L
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end)
    }

    override fun applyTransitionPrev() {
        //STUB, do nothing
    }

}
