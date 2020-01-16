package org.stepik.android.view.attempts.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_attempts.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.presentation.attempts.AttemptsPresenter
import org.stepik.android.presentation.attempts.AttemptsView
import javax.inject.Inject

class AttemptsActivity : FragmentActivityBase(), AttemptsView {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, AttemptsActivity::class.java)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var attemptsPresenter: AttemptsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempts)

        injectComponent()
        attemptsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(AttemptsPresenter::class.java)
        initCenteredToolbar(R.string.attempts_toolbar_title, showHomeButton = true)
        attemptsFeedback.setCompoundDrawables(start = R.drawable.ic_step_quiz_validation)
    }

    private fun injectComponent() {
        App.component()
            .attemptsComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        attemptsPresenter.attachView(this)
    }

    override fun onStop() {
        attemptsPresenter.detachView(this)
        super.onStop()
    }
}