package org.stepic.droid.adaptive.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.Completable
import io.reactivex.Scheduler
import kotlinx.android.synthetic.main.dialog_adaptive_level.view.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.resolveAttribute
import ru.nobird.android.view.base.ui.extension.argument
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AdaptiveLevelDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(level: Long): DialogFragment =
            AdaptiveLevelDialogFragment().apply { this.level = level }
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
        val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        val root = View.inflate(context, R.layout.dialog_adaptive_level, null)
        root.adaptiveLevelDialogTitle.text = level.toString()

        root.continueButton.setOnClickListener { dismiss() }

        expLevelDialogConfetti = root.adaptiveLevelDialogConfetti

        alertDialogBuilder.setView(root)
        return alertDialogBuilder.create()
    }

    override fun onResume() {
        super.onResume()
        val context = expLevelDialogConfetti.context // with theme overlay

        val alpha = context.resolveAttribute(R.attr.alphaEmphasisMedium)?.float ?: return
        val colorOnSurface = context.resolveAttribute(R.attr.colorOnSurface)?.data ?: return
        val colorSecondary = context.resolveAttribute(R.attr.colorSecondary)?.data ?: return

        val hexAlpha = (alpha * 0xFF).toInt() shl 24

        val colors =
            intArrayOf(
                colorOnSurface,
                colorOnSurface and hexAlpha,
                colorSecondary
            )

        Completable
            .timer(0, TimeUnit.MICROSECONDS) // js like work around
            .observeOn(mainScheduler)
            .subscribe {
                CommonConfetti
                    .rainingConfetti(expLevelDialogConfetti, colors)
                    .infinite()
                    .setVelocityY(100f, 30f)
                    .setVelocityX(0f, 60f)
                    .setEmissionRate(15f)
            }
    }
}