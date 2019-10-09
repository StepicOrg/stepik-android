package org.stepic.droid.ui.custom

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import org.stepic.droid.R
import org.stepic.droid.util.ColorUtil

class LanguageTextView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr), Checkable {

    companion object {
        private enum class State(
            @DrawableRes val background: Int,
            @ColorRes val textColor: Int
        ) {
            CHECKED(
                R.drawable.language_checked_background,
                R.color.language_text_color_checked
            ),
            UNCHECKED(
                R.drawable.language_unchecked_background,
                R.color.language_text_color_unchecked
            )
        }
    }


    init {
        isChecked = false
    }

    private var _isChecked: Boolean = false

    override fun isChecked(): Boolean = _isChecked

    override fun toggle() {
        isChecked = !isChecked
    }

    override fun setChecked(checked: Boolean) {
        _isChecked = checked

        val state = when (checked) {
            true -> State.CHECKED
            false -> State.UNCHECKED
        }

        setBackgroundResource(state.background)
        setTextColor(ColorUtil.getColorArgb(state.textColor, context))

        isEnabled = !checked //enabled only unchecked
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ownState = state as SavedState
        super.onRestoreInstanceState(ownState.superState)
        isChecked = ownState.checked
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState() ?: Bundle.EMPTY
        val ownState = SavedState(superState)
        ownState.checked = isChecked
        return ownState
    }

    private class SavedState : View.BaseSavedState {
        var checked: Boolean = false

        private constructor(source: Parcel) : super(source) {
            checked = source.readInt() == 1
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(if (checked) 1 else 0)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)

                override fun newArray(size: Int): Array<SavedState?>? = arrayOfNulls(size)
            }
        }
    }
}
