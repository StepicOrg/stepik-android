package org.stepic.droid.ui.custom

import android.content.Context
import android.support.annotation.StringRes
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.util.TypedValue
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.TextUtil
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import javax.inject.Inject

class PlaceholderTextView : android.support.v7.widget.AppCompatTextView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @Inject
    lateinit var fontsProvider: FontsProvider

    private fun init() {
        App.component().inject(this)

        val sidePadding = resources.getDimensionPixelSize(R.dimen.placeholder_side_padding)
        val topBottomPadding = resources.getDimensionPixelSize(R.dimen.placeholder_top_bottom_padding)
        setPadding(sidePadding, topBottomPadding, sidePadding, topBottomPadding)

        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.placeholder_text_size))

        val defaultLineSpacingMultiplier = 1.0f
        setLineSpacing(resources.getDimension(R.dimen.placeholder_line_spacing_extra), defaultLineSpacingMultiplier)

        setTextColor(ColorUtil.getColorArgb(R.color.placeholder_text_color, this.context))
        setBackgroundResource(R.drawable.gradient_background)
    }


    fun setPlaceholderText(text: CharSequence) {
        val lengthOfFirstWord = TextUtil.getIndexOfFirstSpace(text)

        val result = SpannableString(text)
        val mediumText = CalligraphyTypefaceSpan(TypefaceUtils.load(context.assets, fontsProvider.provideFontPath(FontType.medium)))
        val lightText = CalligraphyTypefaceSpan(TypefaceUtils.load(context.assets, fontsProvider.provideFontPath(FontType.light)))


        result.setSpan(mediumText, 0, lengthOfFirstWord, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        result.setSpan(lightText, lengthOfFirstWord, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        setText(result)
    }

    fun setPlaceholderText(@StringRes textId: Int) {
        val text = resources.getString(textId)
        setPlaceholderText(text)
    }

}
