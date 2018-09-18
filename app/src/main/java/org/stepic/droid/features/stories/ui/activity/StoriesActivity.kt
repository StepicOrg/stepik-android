package org.stepic.droid.features.stories.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.stepic.droid.R
import org.stepic.droid.features.stories.ui.delegate.StoriesActivityDelegate

class StoriesActivity : AppCompatActivity() {
    private lateinit var storiesDelegate: StoriesActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stories)
        storiesDelegate = StoriesActivityDelegate(this)
        storiesDelegate.onCreate(savedInstanceState)
    }

    override fun onPause() {
        storiesDelegate.onPause()
        super.onPause()
    }
}