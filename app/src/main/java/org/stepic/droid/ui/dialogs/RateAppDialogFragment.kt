package org.stepic.droid.ui.dialogs

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
import org.stepic.droid.base.App
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import uk.co.chrisjenx.calligraphy.CalligraphyUtils
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import javax.inject.Inject


class RateAppDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(): RateAppDialogFragment {
            return RateAppDialogFragment()
        }

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
    lateinit var boldTypeface: Typeface

    init {
        App.component().inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        boldTypeface = TypefaceUtils.load(context.assets, fontsProvider.provideFontPath(FontType.bold))
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_rate_app, container, false)
        return v
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false
        CalligraphyUtils.applyFontToTextView(rateDialogPositive, boldTypeface)

        val callback = targetFragment as Callback

        rateDialogLater.setOnClickListener {
            dialog.dismiss()
            callback.onClickLater(rateDialogRatingBar.rating.toInt())
        }

        rateDialogPositive.setOnClickListener {
            dialog.dismiss()
            val rating = rateDialogRatingBar.rating
            if (isExcellent(rating)) {
                callback.onClickGooglePlay(rating.toInt())
            } else {
                callback.onClickSupport(rating.toInt())
            }
        }

        rateDialogRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (!fromUser) {
                return@setOnRatingBarChangeListener
            }

            if (rating == 0f) {
                rateDialogTitle.setText(R.string.rate_dialog_title)
                rateDialogButtonsContainer.visibility = View.GONE
                rateDialogHint.visibility = View.GONE
            } else {
                rateDialogHint.visibility = View.VISIBLE
                rateDialogTitle.setText(R.string.rate_dialog_thanks)
                if (rating > 0f && rating <= 4f) {
                    rateDialogHint.setText(R.string.rate_dialog_hint_negative)
                    rateDialogPositive.setTextAndColor(R.string.rate_dialog_support, R.color.rate_dialog_support)
                } else if (isExcellent(rating)) {
                    rateDialogHint.setText(R.string.rate_dialog_hint_positive)
                    rateDialogPositive.setTextAndColor(R.string.rate_dialog_google_play, R.color.rate_dialog_store)
                }

                rateDialogLater.setTextAndColor(R.string.rate_dialog_later, R.color.stepic_weak_text)
                rateDialogButtonsContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun isExcellent(rating: Float) = rating > 4f

    private fun TextView.setTextAndColor(@StringRes stringRes: Int,
                                         @ColorRes textColorRes: Int) {
        this.setText(stringRes)
        this.setTextColor(org.stepic.droid.util.ColorUtil.getColorArgb(textColorRes, context))
    }

}
