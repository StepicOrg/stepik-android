package org.stepik.android.view.certificates.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.certificates.CertificatesPresenter
import org.stepik.android.presentation.certificates.CertificatesView
import javax.inject.Inject

class CertificatesActivity: FragmentActivityBase(), CertificatesView {
    companion object {
        private const val EXTRA_USER_ID = "user_id"

        fun createIntent(context: Context, userId: Long): Intent =
            Intent(context, CertificatesActivity::class.java)
                .putExtra(EXTRA_USER_ID, userId)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var certificatesPresenter: CertificatesPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certificates)

        injectComponent()
        certificatesPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CertificatesPresenter::class.java)

        initCenteredToolbar(R.string.certificates_title, showHomeButton = true)
    }

    private fun injectComponent() {
        App.component()
            .certificatesComponentBuilder()
            .build()
            .inject(this)
    }

    override fun setState(state: CertificatesView.State) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}