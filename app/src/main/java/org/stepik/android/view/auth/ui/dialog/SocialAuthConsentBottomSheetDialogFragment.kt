package org.stepik.android.view.auth.ui.dialog

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_social_auth_consent.*
import org.stepic.droid.R
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

        private const val ARG_SOCIAL_NETWORK = "social_network"
        private const val ARG_IS_MARKETING_ENABLED = "is_marketing_enabled"

        fun newInstance(socialNetwork: SocialNetwork, isMarketingEnabled: Boolean): DialogFragment =
            SocialAuthConsentBottomSheetDialogFragment().apply {
                this.socialNetwork = socialNetwork
                this.isMarketingEnabled = isMarketingEnabled
            }
    }

    private var socialNetwork: SocialNetwork by argument()
    private var isMarketingEnabled: Boolean by argument()

    private val consentMapper = SocialConsentMapper()

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
            consentErrorText.isVisible = false
        }

        continueButton.setOnClickListener {
            val currentState = RegistrationConsentState(
                isRequiredConsentGranted = requiredConsentCheckBox.isChecked,
                isMarketingVisible = marketingConsentRow.isVisible,
                isMarketingChecked = marketingConsentCheckBox.isChecked
            )

            val result = consentMapper.mapConsent(currentState, socialNetwork)
            if (result is SocialConsentResult.Valid) {
                (activity as? Callback)?.onSocialConsentConfirmed(
                    result.provider,
                    result.pendingConsent
                )
                dismiss()
            } else {
                consentErrorText.isVisible = true
            }
        }
    }

    interface Callback {
        fun onSocialConsentConfirmed(socialNetwork: SocialNetwork, marketingConsent: PendingSocialMarketingConsent)
    }
}
