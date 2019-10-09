package org.stepic.droid.adaptive.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.github.jinatonic.confetti.CommonConfetti
import io.reactivex.Completable
import io.reactivex.Scheduler
import kotlinx.android.synthetic.main.dialog_adaptive_level.view.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.di.qualifiers.MainScheduler
import ru.nobird.android.view.base.ui.extension.argument
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AdaptiveLevelDialog : DialogFragment() {
    companion object {
        fun newInstance(level: Long) =
                AdaptiveLevelDialog().also {
                    it.level = level
                }
    }

    @Inject
    @field:MainScheduler
    lateinit var mainScheduler: Scheduler

    private lateinit var expLevelDialogConfetti: ViewGroup

    private var level by argument<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(requireContext(), R.style.AdaptiveLevelDialogTheme)
        val root = requireActivity().layoutInflater.inflate(R.layout.dialog_adaptive_level, null, false)
        root.adaptiveLevelDialogTitle.text = level.toString()

        root.continueButton.setOnClickListener { dismiss() }

        expLevelDialogConfetti = root.adaptiveLevelDialogConfetti

        alertDialogBuilder.setView(root)
        return alertDialogBuilder.create()
    }

    override fun onResume() {
        super.onResume()
        val context = requireContext()

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