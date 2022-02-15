package org.stepik.android.view.banner.extension

import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.item_banner.view.*
import org.stepic.droid.databinding.ItemBannerBinding
import org.stepik.android.domain.banner.model.Banner
import org.stepik.android.view.banner.mapper.BannerResourcesMapper
import org.stepik.android.view.base.routing.InternalDeeplinkRouter
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

fun ItemBannerBinding.handleItemClick(banner: Banner, fragmentManager: FragmentManager) {
    InternalDeeplinkRouter.openInternalDeeplink(root.context, Uri.parse(banner.url)) {
        InAppWebViewDialogFragment
            .newInstance(banner.title, banner.url, isProvideAuth = false)
            .showIfNotExists(fragmentManager, InAppWebViewDialogFragment.TAG)
    }
}

fun ItemBannerBinding.bind(banner: Banner, bannerResourcesMapper: BannerResourcesMapper) {
    bannerTitle.text = banner.title
    bannerDescription.text = banner.description

    val imageRes = bannerResourcesMapper.mapBannerTypeToImageResource(banner.type)
    val backgroundColorRes = bannerResourcesMapper.mapBannerTypeToBackgroundColor(banner.type)
    val descriptionTextColorRes = bannerResourcesMapper.mapBannerTypeToDescriptionTextColor(banner.type)

    bannerImage.setImageResource(imageRes)
    ViewCompat.setBackgroundTintList(root.bannerRoot, AppCompatResources.getColorStateList(root.context, backgroundColorRes))
    bannerDescription.setTextColor(descriptionTextColorRes)
}