package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.dialog_progress.*
import org.stepic.droid.R

class LoadingProgressDialog
@JvmOverloads
constructor(
    context: Context,
    @StringRes private val titleRes: Int = R.string.loading
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_progress)
        progressDialogTitle.setText(titleRes)
        progressDialogMessage.setText(R.string.loading_message)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }
}