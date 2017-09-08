package org.stepic.droid.ui.activities

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import kotlinx.android.synthetic.main.activity_login_new.*
import org.stepic.droid.fonts.FontType
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan
import uk.co.chrisjenx.calligraphy.TypefaceUtils


class LoginActivityNew : FragmentActivityBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_new)

        val signInString = getString(R.string.sign_in)
        val signInWithSocial = getString(R.string.sign_in_with_password_suffix)

        val spannableSignIn = SpannableString(signInString + signInWithSocial)
        val typefaceSpan = CalligraphyTypefaceSpan(TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.medium)))

        spannableSignIn.setSpan(typefaceSpan, 0, signInString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        signInText.text = spannableSignIn

        loginButton.setOnClickListener {
            // to make ripple work
        }

    }

}