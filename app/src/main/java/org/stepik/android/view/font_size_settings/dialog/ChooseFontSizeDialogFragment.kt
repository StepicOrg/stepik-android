package org.stepik.android.view.font_size_settings.dialog

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.font_size_settings.FontSizePresenter
import javax.inject.Inject

class ChooseFontSizeDialogFragment: DialogFragment() {
    companion object {
        const val TAG = "ChooseFontSizeDialogFragment"

        fun newInstance(): DialogFragment =
            ChooseFontSizeDialogFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: FontSizePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        presenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(FontSizePresenter::class.java)
    }

    private fun injectComponent() {
        App.component()
            .fontSizeComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_choose_font_size, container, false)

}