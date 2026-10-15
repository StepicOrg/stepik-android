package org.stepik.android.view.debug.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Block
import org.stepik.android.model.Step
import org.stepik.android.model.code.CodeLimit
import org.stepik.android.model.code.CodeOptions
import org.stepik.android.model.util.ParcelableStringList
import org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog.CodeStepQuizFullScreenDialogFragment

object CodeStepQuizFullScreenPreviewData {
    private const val PYTHON = "python3"
    private const val JAVA = "java"

    fun show(host: Fragment) {
        val fragmentManager = host.childFragmentManager
        if (fragmentManager.findFragmentByTag(CodeStepQuizFullScreenDialogFragment.TAG) != null) return

        val dialog = createDialog()
        val callback = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?) {
                if (fragment !== dialog) return
                fm.unregisterFragmentLifecycleCallbacks(this)

                // Keep the run tab visible, but prevent the sample step from reaching the API.
                view.findViewById<ViewPager>(R.id.fullScreenCodeViewPager).offscreenPageLimit = 2
                view.post {
                    val onRunClick = View.OnClickListener {
                        Toast.makeText(view.context, R.string.debug_code_step_quiz_run_preview_message, Toast.LENGTH_SHORT).show()
                    }
                    view.findViewById<View>(R.id.runCodeAction)?.setOnClickListener(onRunClick)
                    view.findViewById<View>(R.id.runCodeFab)?.setOnClickListener(onRunClick)
                }
            }
        }
        fragmentManager.registerFragmentLifecycleCallbacks(callback, false)
        dialog.show(fragmentManager, CodeStepQuizFullScreenDialogFragment.TAG)
    }

    private fun createDialog(): DialogFragment {
        val codeTemplates = mapOf(
            PYTHON to "a = int(input())\nprint(a * 2)",
            JAVA to "class Main {\n    public static void main(String[] args) {\n        System.out.println(42);\n    }\n}"
        )
        val sample = ParcelableStringList().apply {
            add("21")
            add("42")
        }
        val options = CodeOptions(
            limits = mapOf(PYTHON to CodeLimit(2, 64), JAVA to CodeLimit(3, 128)),
            executionTimeLimit = 2,
            codeTemplates = codeTemplates,
            executionMemoryLimit = 64,
            samples = listOf(sample),
            isRunUserCodeAllowed = true
        )
        val step = Step(
            id = -1L,
            lesson = -1L,
            position = 1L,
            block = Block(
                name = AppConstants.TYPE_CODE,
                text = "Read an integer and print twice its value.",
                options = options
            )
        )

        return CodeStepQuizFullScreenDialogFragment.newInstance(
            lang = PYTHON,
            code = codeTemplates.getValue(PYTHON),
            codeTemplates = codeTemplates,
            stepPersistentWrapper = StepPersistentWrapper(step),
            lessonTitle = "Sample code lesson"
        )
    }
}
