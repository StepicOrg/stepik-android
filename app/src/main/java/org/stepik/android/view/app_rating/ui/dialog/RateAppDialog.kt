package org.stepik.android.view.app_rating.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.DialogRateAppBinding
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.util.RatingUtil
import org.stepic.droid.util.reportRateEvent
import org.stepic.droid.util.resolveColorAttribute
import javax.inject.Inject

class RateAppDialog : DialogFragment() {

    companion object {
        const val TAG = "rate_app_dialog"
        private const val ratingKey = "ratingKey"

        fun newInstance(): RateAppDialog =
            RateAppDialog()

        /**
         * This callback should be implemented by targeted fragment
         */
        interface Callback {
            fun onClickLater(starNumber: Int)

            fun onClickGooglePlay(starNumber: Int)

            fun onClickSupport(starNumber: Int)
        }
    }
    private val rateAppBinding: DialogRateAppBinding by viewBinding(DialogRateAppBinding::bind)

    @Inject
    lateinit var analytic: Analytic

    init {
        App.component().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_rate_app, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        val callback = targetFragment as? Callback
            ?: activity as Callback

        rateAppBinding.rateDialogLater.setOnClickListener {
            dialog?.dismiss()
            callback.onClickLater(rateAppBinding.rateDialogRatingBar.rating.toInt())
        }

        rateAppBinding.rateDialogPositive.setOnClickListener {
            dialog?.dismiss()
            val rating = rateAppBinding.rateDialogRatingBar.rating.toInt()
            if (RatingUtil.isExcellent(rating)) {
                callback.onClickGooglePlay(rating)
            } else {
                callback.onClickSupport(rating)
            }
        }

        rateAppBinding.rateDialogRatingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (!fromUser) {
                return@setOnRatingBarChangeListener
            }

            val starNumber = rating.toInt()
            analytic.reportRateEvent(starNumber, Analytic.Rating.APP_RATE)
            applyRating(rating = starNumber)
        }

        savedInstanceState?.let {
            val rating = it.getInt(ratingKey)
            applyRating(rating)
        }
    }

    private fun applyRating(rating: Int) {
        if (rating == 0) {
            rateAppBinding.rateDialogTitle.setText(R.string.rate_dialog_title)
            rateAppBinding.rateDialogButtonsContainer.visibility = View.GONE
            rateAppBinding.rateDialogHint.visibility = View.GONE
        } else {
            rateAppBinding.rateDialogHint.visibility = View.VISIBLE
            rateAppBinding.rateDialogTitle.setText(R.string.rate_dialog_thanks)

            if (rating in 1..4) {
                rateAppBinding.rateDialogHint.setText(R.string.rate_dialog_hint_negative)
                rateAppBinding.rateDialogPositive.setTextAndColor(R.string.rate_dialog_support, R.attr.colorError)
            } else if (RatingUtil.isExcellent(rating)) {
                rateAppBinding.rateDialogHint.setText(R.string.rate_dialog_hint_positive)
                rateAppBinding.rateDialogPositive.setTextAndColor(R.string.rate_dialog_google_play, R.attr.colorSecondary)
            }

            rateAppBinding.rateDialogButtonsContainer.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ratingKey, rateAppBinding.rateDialogRatingBar.rating.toInt())
    }

    private fun TextView.setTextAndColor(@StringRes stringRes: Int, @AttrRes textColorRes: Int) {
        this.setText(stringRes)
        this.setTextColor(this.context.resolveColorAttribute(textColorRes))
    }
}