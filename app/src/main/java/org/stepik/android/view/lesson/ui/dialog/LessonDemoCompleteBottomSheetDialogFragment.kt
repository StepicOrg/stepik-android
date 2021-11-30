package org.stepik.android.view.lesson.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_lesson_demo_complete.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepik.android.data.course.repository.CoursePurchaseDataRepositoryImpl
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course.resolver.CoursePromoCodeResolver
import org.stepik.android.view.course.routing.CourseScreenTab
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class LessonDemoCompleteBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "LessonDemoCompleteBottomSheetDialog"

        fun newInstance(course: Course): DialogFragment =
            LessonDemoCompleteBottomSheetDialogFragment().apply {
                this.course = course
            }
    }

    private var course: Course by argument()

    @Inject
    lateinit var screenManager: ScreenManager

    @Inject
    lateinit var displayPriceMapper: DisplayPriceMapper

    @Inject
    lateinit var coursePromoCodeResolver: CoursePromoCodeResolver

    @Inject
    lateinit var coursePurchaseDataRepository: CoursePurchaseDataRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.componentManager().courseComponent(course.id).inject(this)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TopCornersRoundedBottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_lesson_demo_complete, container, false)

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        demoCompleteTitle.text = getString(R.string.demo_complete_title, course.title)

        if (coursePurchaseDataRepository.coursePurchaseData != null) {
            setupIAP(coursePurchaseDataRepository.coursePurchaseData!!)
        } else {
            setupWeb(coursePurchaseDataRepository.deeplinkPromoCode)
        }
    }

    private fun setupWeb(deeplinkPromoCode: DeeplinkPromoCode) {
        val courseDisplayPrice = course.displayPrice
        val (_, currencyCode, promoPrice, hasPromo) = coursePromoCodeResolver.resolvePromoCodeInfo(
            deeplinkPromoCode,
            DefaultPromoCode(
                course.defaultPromoCodeName ?: "",
                course.defaultPromoCodePrice ?: "",
                course.defaultPromoCodeDiscount ?: "",
                course.defaultPromoCodeExpireDate
            ),
            course
        )
        demoCompleteAction.text =
            if (courseDisplayPrice != null) {
                if (hasPromo) {
                    displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(courseDisplayPrice, promoPrice, currencyCode)
                } else {
                    getString(R.string.course_payments_purchase_in_web_with_price, courseDisplayPrice)
                }
            } else {
                getString(R.string.course_payments_purchase_in_web)
            }

        demoCompleteAction.setOnClickListener {
            screenManager.showCoursePurchaseFromLessonDemoDialog(requireContext(), course.id, CourseViewSource.LessonDemoDialog, CourseScreenTab.INFO, deeplinkPromoCode)
        }
    }

    private fun setupIAP(coursePurchaseData: CoursePurchaseData) {
        val courseDisplayPrice = coursePurchaseData.course.displayPrice
        demoCompleteAction.text =
            if (courseDisplayPrice != null) {
                if (coursePurchaseData.promoCodeSku.lightSku != null) {
                    displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(coursePurchaseData.primarySku.price, coursePurchaseData.promoCodeSku.lightSku.price)
                } else {
                    getString(R.string.course_payments_purchase_in_web_with_price, coursePurchaseData.primarySku.price)
                }
            } else {
                getString(R.string.course_payments_purchase_in_web)
            }
    }
}