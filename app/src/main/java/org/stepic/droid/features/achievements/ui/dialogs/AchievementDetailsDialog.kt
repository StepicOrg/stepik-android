package org.stepic.droid.features.achievements.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import kotlinx.android.synthetic.main.dialog_achievement_details.view.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.achievements.AchievementFlatItem
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.wrapWithGlide
import org.stepic.droid.util.argument
import javax.inject.Inject

class AchievementDetailsDialog: DialogFragment() {
    companion object {
        const val TAG = "achievement_details_dialog"

        fun newInstance(achievementFlatItem: AchievementFlatItem, canShareAchievement: Boolean): AchievementDetailsDialog =
                AchievementDetailsDialog().apply {
                    achievementItem = achievementFlatItem
                    canShare = canShareAchievement
                }
    }

    private var achievementItem: AchievementFlatItem by argument()
    private var canShare: Boolean by argument()

    @Inject
    lateinit var achievementResourceResolver: AchievementResourceResolver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_achievement_details, null, false)

        view.apply {
            achievementTitle.text = achievementResourceResolver.resolveTitleForKind(achievementItem.kind)
            achievementDescription.text = achievementResourceResolver.resolveDescription(achievementItem)
            achievementIcon.apply {
                wrapWithGlide().setImagePath(achievementResourceResolver.resolveAchievementIcon(achievementItem, this))
            }

            achievementLevelProgress.progress = achievementItem.currentScore.toFloat() / achievementItem.targetScore
            achievementLevels.progress = achievementItem.currentLevel
            achievementLevels.total = achievementItem.maxLevel

            achievementLevel.text = getString(R.string.achievement_level, achievementItem.currentLevel, achievementItem.maxLevel)

            val scoreDiff = achievementItem.targetScore - achievementItem.currentScore
            achievementRest.text = if (achievementItem.isLocked) {
                getString(R.string.achievement_remaining_exp_locked)
            } else {
                getString(R.string.achievement_remaining_exp, scoreDiff)
            }
            achievementRest.changeVisibility(scoreDiff > 0)
        }

        val builder = MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .customView(view, false)

        if (canShare && !achievementItem.isLocked) {
            builder
                .positiveText(R.string.share_title)
                .negativeText(R.string.close_screen)
                .negativeColorRes(R.color.new_accent_color_opacity_50)
                .onPositive { _, _ -> shareAchievement() }
        }

        return builder.build()
    }

    override fun onStart() {
        super.onStart()
        dialog.window.attributes = dialog.window.attributes.apply {
            width = context.resources.getDimension(R.dimen.achievement_details_dialog_width).toInt()
        }
    }

    private fun shareAchievement() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, getString(R.string.achievement_share, achievementResourceResolver.resolveTitleForKind(achievementItem.kind)))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "text/plain"
        }

        activity.startActivity(Intent.createChooser(intent, getString(R.string.share_title)))
    }
}