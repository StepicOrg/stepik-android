package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Unit
import org.stepic.droid.ui.fragments.StepsFragment
import org.stepic.droid.util.AppConstants

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
        val unit = intent.extras.get(AppConstants.KEY_UNIT_BUNDLE) as Unit
        val lesson = intent.extras.get(AppConstants.KEY_LESSON_BUNDLE) as Lesson
        val fromPrevious : Boolean = intent?.extras?.getBoolean(needReverseAnimationKey, false) ?: false
        return StepsFragment.newInstance(unit, lesson, fromPrevious)
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
