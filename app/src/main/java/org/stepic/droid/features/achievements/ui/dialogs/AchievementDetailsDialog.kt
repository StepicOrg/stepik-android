package org.stepic.droid.features.achievements.ui.dialogs

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.dialog_achievement_details.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.achievements.AchievementFlatItem
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.argument
import org.stepic.droid.util.svg.GlideSvgRequestFactory
import javax.inject.Inject

class AchievementDetailsDialog: DialogFragment() {
    companion object {
        const val TAG = "achievement_details_dialog"

        fun newInstance(achievementFlatItem: AchievementFlatItem): AchievementDetailsDialog =
                AchievementDetailsDialog().apply { achievementItem = achievementFlatItem }
    }

    private var achievementItem by argument<AchievementFlatItem>()

    private val svgRequestBuilder by lazy {
        GlideSvgRequestFactory
                .create(context, null)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
    }

    @Inject
    lateinit var achievementResourceResolver: AchievementResourceResolver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_achievement_details, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAchievementIcon(achievementResourceResolver.resolveAchievementIcon(achievementItem, achievementIcon))
    }

    private fun setAchievementIcon(path: String) {
        if (path.endsWith(AppConstants.SVG_EXTENSION)) {
            svgRequestBuilder
                    .load(Uri.parse(path))
                    .into(achievementIcon)
        } else {
            Glide.with(context)
                    .load(path)
                    .asBitmap()
                    .into(achievementIcon)
        }
    }
}