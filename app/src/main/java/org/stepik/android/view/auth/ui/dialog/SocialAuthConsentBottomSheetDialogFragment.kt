package org.stepik.android.view.auth.ui.dialog

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_social_auth_consent.*
import org.stepic.droid.R
import org.stepic.droid.util.resolveColorAttribute
import org.stepic.droid.util.stripUnderlinesFromLinks
import org.stepik.android.domain.auth.mapper.SocialConsentMapper
import org.stepik.android.domain.auth.mapper.SocialConsentResult
import org.stepik.android.domain.auth.model.PendingSocialMarketingConsent
import org.stepik.android.domain.auth.model.RegistrationConsentState
import org.stepik.android.view.auth.model.SocialNetwork
import ru.nobird.android.view.base.ui.extension.argument

class SocialAuthConsentBottomSheetDialogFragment :
    BottomSheetDialogFragment() {

    companion object {
        const val TAG = "SocialAuthConsentBottomSheetDialogFragment"

        fun newInstance(socialNetwork: SocialNetwork, isMarketingEnabled: Boolean): DialogFragment =
            SocialAuthConsentBottomSheetDialogFragment().apply {
                this.socialNetwork = socialNetwork
                this.isMarketingEnabled = isMarketingEnabled
            }
    }

    private var socialNetwork: SocialNetwork by argument()
    private var isMarketingEnabled: Boolean by argument()
    private val requiredConsentDefaultTint by lazy {
        createRequiredConsentTint(
            checkedColor = requireContext().resolveColorAttribute(R.attr.colorSecondary),
            uncheckedColor = requireContext().resolveColorAttribute(R.attr.colorOnSurface)
        )
    }
    private val requiredConsentErrorTint by lazy {
        createRequiredConsentTint(
            checkedColor = requireContext().resolveColorAttribute(R.attr.colorSecondary),
            uncheckedColor = requireContext().resolveColorAttribute(R.attr.colorError)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TopCornersRoundedBottomSheetDialog)
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_social_auth_consent, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val consentRequiredHtml = getString(R.string.registration_consent_required)
        requiredConsentText.movementMethod = LinkMovementMethod.getInstance()
        requiredConsentText.text = HtmlCompat.fromHtml(consentRequiredHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        stripUnderlinesFromLinks(requiredConsentText)

        if (isMarketingEnabled) {
            marketingConsentRow.isVisible = true
            marketingHelperText.isVisible = true
            marketingConsentCheckBox.isChecked = true
        }

        requiredConsentCheckBox.setOnCheckedChangeListener { _, _ ->
            clearRequiredConsentError()
        }
        clearRequiredConsentError()

        continueButton.setOnClickListener {
            val currentState = RegistrationConsentState(
                isRequiredConsentGranted = requiredConsentCheckBox.isChecked,
                isMarketingVisible = marketingConsentRow.isVisible,
                isMarketingChecked = marketingConsentCheckBox.isChecked
            )

            val result = SocialConsentMapper.mapConsent(currentState, socialNetwork)
            if (result is SocialConsentResult.Valid) {
                (activity as? Callback)?.onSocialConsentConfirmed(
                    result.provider,
                    result.pendingConsent
                )
                dismiss()
            } else {
                showRequiredConsentError()
            }
        }
    }

    private fun showRequiredConsentError() {
        consentErrorText.isVisible = true
        CompoundButtonCompat.setButtonTintList(
            requiredConsentCheckBox,
            requiredConsentErrorTint
        )
    }

    private fun clearRequiredConsentError() {
        consentErrorText.isVisible = false
        CompoundButtonCompat.setButtonTintList(
            requiredConsentCheckBox,
            requiredConsentDefaultTint
        )
    }

    private fun createRequiredConsentTint(checkedColor: Int, uncheckedColor: Int): ColorStateList =
        ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                checkedColor,
                uncheckedColor
            )
        )

    interface Callback {
        fun onSocialConsentConfirmed(socialNetwork: SocialNetwork, marketingConsent: PendingSocialMarketingConsent)
    }
}
