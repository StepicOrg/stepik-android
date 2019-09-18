package org.stepik.android.view.font_size_settings.ui.dialog

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_choose_font_size.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.font_size_settings.FontSizePresenter
import org.stepik.android.presentation.font_size_settings.FontSizeView
import org.stepik.android.view.font_size_settings.model.FontItem
import org.stepik.android.view.font_size_settings.ui.adapter.FontSizeDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter
import ru.nobird.android.ui.adapterssupport.selection.SingleChoiceSelectionHelper
import javax.inject.Inject

class ChooseFontSizeDialogFragment : DialogFragment(), FontSizeView {
    companion object {
        const val TAG = "ChooseFontSizeDialogFragment"

        fun newInstance(): DialogFragment =
            ChooseFontSizeDialogFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: FontSizePresenter

    private val fontsAdapter: DefaultDelegateAdapter<FontItem> = DefaultDelegateAdapter()
    private lateinit var selectionHelper: SingleChoiceSelectionHelper

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fontsAdapter.items = listOf(
            FontItem("Small", FontItem.Size.SMALL),
            FontItem("Medium", FontItem.Size.MEDIUM),
            FontItem("Large", FontItem.Size.LARGE)
        )

        selectionHelper = SingleChoiceSelectionHelper(fontsAdapter)
        fontsAdapter += FontSizeDelegate(selectionHelper) { selectionHelper.select(fontsAdapter.items.indexOf(it)) }
        fontSizeRecycler.layoutManager = LinearLayoutManager(requireContext())
        fontSizeRecycler.adapter = fontsAdapter

        dialogNegativeButton.setOnClickListener { dismiss() }
        dialogPositiveButton.setOnClickListener {
            val selectedPosition = (0 until fontsAdapter.itemCount).find { selectionHelper.isSelected(it) } ?: 0
            presenter.onFontSizeChosen(fontsAdapter.items[selectedPosition].fontSize.size)
        }
        presenter.fetchFontSize()
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }

    override fun onFontSizeChosen() {
        dismiss()
    }

    override fun setCachedFontSize(fontSize: Float) {
        selectionHelper.select(fontsAdapter.items.indexOfFirst { it.fontSize.size ==  fontSize })
    }
}