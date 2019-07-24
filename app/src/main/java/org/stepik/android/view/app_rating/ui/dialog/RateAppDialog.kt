package org.stepik.android.view.app_rating.ui.dialog

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_rate_app.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.util.RatingUtil
import org.stepic.droid.util.reportRateEvent
import uk.co.chrisjenx.calligraphy.CalligraphyUtils
import uk.co.chrisjenx.calligraphy.TypefaceUtils
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

    @Inject
    lateinit var fontsProvider: FontsProvider
    @Inject
    lateinit var analytic: Analytic

    private lateinit var boldTypeface: Typeface

    init {
        App.component().inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        boldTypeface = TypefaceUtils.load(context.assets, fontsProvider.provideFontPath(FontType.bold))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_rate_app, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false
        CalligraphyUtils.applyFontToTextView(rateDialogPositive, boldTypeface)

        val callback = if (targetFragment != null) {
            targetFragment as Callback
        } else {
            activity as Callback
        }

        rateDialogLater.setOnClickListener {
            dialog.dismiss()
            callback.onClickLater(rateDialogRatingBar.rating.toInt())
        }

        rateDialogPositive.setOnClickListener {
            dialog.dismiss()
            val rating = rateDialogRatingBar.rating.toInt()
            if (RatingUtil.isExcellent(rating)) {
                callback.onClickGooglePlay(rating)
            } else {
                callback.onClickSupport(rating)
            }
        }

        rateDialogRatingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
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
            rateDialogTitle.setText(R.string.rate_dialog_title)
            rateDialogButtonsContainer.visibility = View.GONE
            rateDialogHint.visibility = View.GONE
        } else {
            rateDialogHint.visibility = View.VISIBLE
            rateDialogTitle.setText(R.string.rate_dialog_thanks)
            if (rating in 1..4) {
                rateDialogHint.setText(R.string.rate_dialog_hint_negative)
                rateDialogPositive.setTextAndColor(R.string.rate_dialog_support, R.color.rate_dialog_support)
            } else if (RatingUtil.isExcellent(rating)) {
                rateDialogHint.setText(R.string.rate_dialog_hint_positive)
                rateDialogPositive.setTextAndColor(R.string.rate_dialog_google_play, R.color.rate_dialog_store)
            }

            rateDialogLater.setTextAndColor(R.string.rate_dialog_later, R.color.stepic_weak_text)
            rateDialogButtonsContainer.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ratingKey, rateDialogRatingBar.rating.toInt())
    }

    private fun TextView.setTextAndColor(@StringRes stringRes: Int, @ColorRes textColorRes: Int) {
        this.setText(stringRes)
        this.setTextColor(org.stepic.droid.util.ColorUtil.getColorArgb(textColorRes, context))
    }
}