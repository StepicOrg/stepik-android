package org.stepic.droid.features.course.ui.activity

import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_course.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Course

class CourseActivity : FragmentActivityBase() {

    private val course by lazy { intent.getParcelableExtra<Course>(AppConstants.KEY_COURSE_BUNDLE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        setSupportActionBar(courseToolbar)
        val actionBar = this.supportActionBar
                ?: throw IllegalStateException("support action bar should be set")

        with(actionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        Glide.with(this)
                .load(config.baseUrl + course.cover)
                .bitmapTransform(CenterCrop(this), BlurTransformation(this))
                .into(courseCover)
    }
}