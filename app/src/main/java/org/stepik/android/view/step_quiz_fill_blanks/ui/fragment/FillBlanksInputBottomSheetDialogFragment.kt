package org.stepik.android.view.step_quiz_fill_blanks.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_fill_blanks_input.*
import org.stepic.droid.R
import ru.nobird.android.view.base.ui.extension.argument

class FillBlanksInputBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "FillBlanksInputBottomSheetDialogFragment"

        private const val ARG_INDEX = "INDEX"
        private const val ARG_TEXT = "TEXT"

        fun newInstance(index: Int, text: String): DialogFragment =
            FillBlanksInputBottomSheetDialogFragment()
                .apply {
                    this.index = index
                    this.text = text
                }
    }

    private var index: Int by argument()
    private var text: String by argument()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_fill_blanks_input, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt(ARG_INDEX)
            text = savedInstanceState.getString(ARG_TEXT) ?: return
        }
        fillBlanksInputField.setText(text)
    }

    override fun onPause() {
        (parentFragment as? Callback)
            ?.onSyncInputItemWithParent(index, fillBlanksInputField.text.toString())
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_INDEX, index)
        outState.putString(ARG_TEXT, text)
    }

    interface Callback {
        fun onSyncInputItemWithParent(index: Int, text: String)
    }
}