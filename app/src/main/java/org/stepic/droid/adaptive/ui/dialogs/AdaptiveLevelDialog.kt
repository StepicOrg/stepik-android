package org.stepic.droid.adaptive.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import com.github.jinatonic.confetti.CommonConfetti
import io.reactivex.Completable
import io.reactivex.Scheduler
import kotlinx.android.synthetic.main.dialog_adaptive_level.view.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.di.qualifiers.MainScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AdaptiveLevelDialog : DialogFragment() {
    companion object {
        private const val LEVEL_KEY = "level"

        fun newInstance(level: Long) : AdaptiveLevelDialog {
            val dialog = AdaptiveLevelDialog()
            val args = Bundle()
            args.putLong(LEVEL_KEY, level)
            dialog.arguments = args
            return dialog
        }
    }

    @Inject
    @field:MainScheduler
    lateinit var mainScheduler: Scheduler

    private lateinit var expLevelDialogConfetti: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.AdaptiveLevelDialogTheme)
        val root = activity.layoutInflater.inflate(R.layout.dialog_adaptive_level, null, false)
        root.adaptiveLevelDialogTitle.text = arguments.getLong(LEVEL_KEY).toString()

        root.continueButton.setOnClickListener { dismiss() }

        expLevelDialogConfetti = root.adaptiveLevelDialogConfetti

        alertDialogBuilder.setView(root)
        return alertDialogBuilder.create()
    }

    override fun onResume() {
        super.onResume()
        Completable
                .timer(0, TimeUnit.MICROSECONDS) // js like work around
                .observeOn(mainScheduler).subscribe {
            CommonConfetti.rainingConfetti(expLevelDialogConfetti, intArrayOf(
                    Color.BLACK,
                    ContextCompat.getColor(context, R.color.pressed_white),
                    ContextCompat.getColor(context, R.color.adaptive_color_correct)
            )).infinite().setVelocityY(100f, 30f).setVelocityX(0f, 60f).setEmissionRate(15f)
        }
    }
}