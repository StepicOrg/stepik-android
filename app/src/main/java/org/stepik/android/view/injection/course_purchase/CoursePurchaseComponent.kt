package org.stepik.android.view.injection.course_purchase

import dagger.Subcomponent
import org.stepik.android.view.course_purchase.ui.dialog.CoursePurchaseBottomSheetDialogFragment
import org.stepik.android.view.injection.feedback.FeedbackDataModule
import org.stepik.android.view.injection.wishlist.WishlistDataModule

@Subcomponent(modules = [
    CoursePurchasePresentationModule::class,
    WishlistDataModule::class,
    FeedbackDataModule::class
])
interface CoursePurchaseComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CoursePurchaseComponent
    }

    fun inject(coursePurchaseBottomSheetDialogFragment: CoursePurchaseBottomSheetDialogFragment)
}