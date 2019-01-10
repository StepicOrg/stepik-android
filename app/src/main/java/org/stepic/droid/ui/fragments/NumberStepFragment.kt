package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.text.InputType
import android.view.View
import org.stepik.android.model.Reply

class NumberStepFragment: SingleLineSendStepFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        answerField.setRawInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
    }

    override fun generateReply(): Reply =
            Reply(number = answerField.text.toString())

    override fun onRestoreSubmission() {
        val text = submission.reply?.number ?: return
        answerField.setText(text)
    }
}