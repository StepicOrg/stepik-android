package org.stepic.droid.ui.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_rate_app.*
import org.stepic.droid.R
import timber.log.Timber


class RateAppDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(): RateAppDialogFragment {
            return RateAppDialogFragment()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_rate_app, container, false)
//        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
//        v.layoutParams = layoutParams
        return v
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rateDialogRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (!fromUser) {
                return@setOnRatingBarChangeListener
            }

            Timber.d("rating $rating")
            if (rating == 0f) {
                rateDialogButtonsContainer.visibility = View.GONE
            } else
                if (rating > 0f && rating < 5) {
                    rateDialogLater.text = "Later"
                    rateDialogPositive.text = "Support"
                    rateDialogButtonsContainer.visibility = View.VISIBLE
                } else if (rating == 5f) {
                    rateDialogLater.text = "Later"
                    rateDialogPositive.text = "Google Play"
                    rateDialogButtonsContainer.visibility = View.VISIBLE
                }
        }
    }

}
