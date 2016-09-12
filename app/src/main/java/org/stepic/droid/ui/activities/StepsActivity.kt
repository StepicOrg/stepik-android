package org.stepic.droid.ui.activities

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Unit
import org.stepic.droid.ui.fragments.StepsFragment
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.HtmlHelper

class StepsActivity : SingleFragmentActivity() {

    companion object {
        val needReverseAnimationKey = "needReverseAnimation"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val isNeedReverseAnimation = intent?.extras?.getBoolean(needReverseAnimationKey, false)
            if (isNeedReverseAnimation ?: false) {
                overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end)
            } else {
                overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start)
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun createFragment(): Fragment {
        val extras = intent.extras

        val unit = extras?.get(AppConstants.KEY_UNIT_BUNDLE) as? Unit //Unit can be null
        val lesson = extras?.get(AppConstants.KEY_LESSON_BUNDLE) as? Lesson //Lesson can be null in intent and if url is broken
        val fromPrevious: Boolean = extras?.getBoolean(needReverseAnimationKey, false) ?: false

        val dataUri = intent?.data

        if (lesson == null && dataUri != null) {
            analytic.reportEvent(Analytic.DeepLink.USER_OPEN_STEPS_LINK);
            //All can be -1
            val simpleLessonId: Long = getSimpleLessonId(dataUri)
            val simpleStepPosition: Long = getStepPosition(dataUri)
            val simpleUnitId: Long = getUnitSimpleId(dataUri)
            return StepsFragment.newInstance(simpleUnitId, simpleLessonId, simpleStepPosition)

        } else {
            return StepsFragment.newInstance(unit, lesson, fromPrevious)
        }
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

    private fun parseLong(raw: String?): Long {
        if (raw == null) {
            return -1L;
        } else {
            try {
                return java.lang.Long.parseLong(raw)
            } catch (numberFormatException: NumberFormatException) {
                return -1L
            }
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
        //STUB
    }

}
