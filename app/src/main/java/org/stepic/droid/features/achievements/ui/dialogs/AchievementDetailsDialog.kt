package org.stepic.droid.features.achievements.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import kotlinx.android.synthetic.main.dialog_achievement_details.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.achievements.AchievementFlatItem
import org.stepic.droid.ui.util.wrapWithGlide
import org.stepic.droid.util.argument
import javax.inject.Inject

class AchievementDetailsDialog: DialogFragment() {
    companion object {
        const val TAG = "achievement_details_dialog"

        fun newInstance(achievementFlatItem: AchievementFlatItem): AchievementDetailsDialog =
                AchievementDetailsDialog().apply { achievementItem = achievementFlatItem }
    }

    private var achievementItem by argument<AchievementFlatItem>()

    @Inject
    lateinit var achievementResourceResolver: AchievementResourceResolver

    private val achievementIconWrapper by lazy { achievementIcon.wrapWithGlide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_achievement_details, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        achievementIconWrapper.setImagePath(achievementResourceResolver.resolveAchievementIcon(achievementItem, achievementIcon))
        achievementTitle.text = achievementResourceResolver.resolveTitleForKind(achievementItem.kind)
        achievementDescription.text = achievementResourceResolver.resolveDescription(achievementItem)


    }

    override fun onStart() {
        super.onStart()
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}