package org.stepik.android.view.font_size_settings.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.widget.ArrayAdapter
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepik.android.domain.step_content_text.model.FontSize
import org.stepik.android.presentation.font_size_settings.FontSizePresenter
import org.stepik.android.presentation.font_size_settings.FontSizeView
import java.util.Locale
import javax.inject.Inject

class ChooseFontSizeDialogFragment : DialogFragment(), FontSizeView {
    companion object {
        const val TAG = "ChooseFontSizeDialogFragment"

        fun newInstance(): DialogFragment =
            ChooseFontSizeDialogFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var analytic: Analytic

    private lateinit var presenter: FontSizePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        presenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(FontSizePresenter::class.java)
        presenter.fetchFontSize()
    }

    private fun injectComponent() {
        App.component()
            .fontSizeComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog
            .Builder(requireContext())
            .setTitle(R.string.setting_font_size_dialog_title)
            .setSingleChoiceItems(
                ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_single_choice, resources.getStringArray(R.array.step_content_font_size)),
                -1
            ) { _, which ->
                val fontSize = FontSize.values()[which]
                presenter.onFontSizeChosen(fontSize)
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.FontSize.FONT_SIZE_SELECTED, mapOf(
                    AmplitudeAnalytic.FontSize.Params.SIZE to fontSize.name.toLowerCase(Locale.US)
                ))
                analytic.reportEvent(Analytic.FontSize.FONT_SIZE_SELECTED, Bundle().apply {
                    putString(Analytic.FontSize.Params.SIZE, fontSize.name.toLowerCase(Locale.US))
                })
                dismiss()
            }
            .create()

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }

    override fun setCachedFontSize(fontSize: FontSize) {
        (dialog as AlertDialog).listView.setItemChecked(fontSize.ordinal, true)
    }
}