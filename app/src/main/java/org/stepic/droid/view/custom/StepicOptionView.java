package org.stepic.droid.view.custom;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.stepic.droid.R;
import org.stepic.droid.util.AppConstants;

public abstract class StepicOptionView extends LinearLayout implements Checkable {

    private ImageView optionIcon;

    private WebView optionText;


    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    private boolean isChecked;

    public StepicOptionView(Context context) {
        this(context, null);
    }

    public StepicOptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepicOptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.stepic_compound_button, this, true);
        optionIcon = (ImageView) findViewById(R.id.image_compound_button);
        optionText = (WebView) findViewById(R.id.text_compound_button);
        init();
    }

    private void init() {
        setClickable(true);
        optionText.setClickable(true);
        optionText.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        optionText.setOnTouchListener(new OnTouchListener() {
            public final static int FINGER_RELEASED = 0;
            public final static int FINGER_TOUCHED = 1;
            public final static int FINGER_DRAGGING = 2;
            public final static int FINGER_UNDEFINED = 3;

            private int fingerState = FINGER_RELEASED;


            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        if (fingerState == FINGER_RELEASED) fingerState = FINGER_TOUCHED;
                        else fingerState = FINGER_UNDEFINED;
                        break;

                    case MotionEvent.ACTION_UP:
                        if (fingerState != FINGER_DRAGGING) {
                            fingerState = FINGER_RELEASED;

                            StepicOptionView parent = (StepicOptionView) v.getParent().getParent(); //// FIXME: 27.04.16 find better way for handling
                            parent.performClick();

                        } else if (fingerState == FINGER_DRAGGING) fingerState = FINGER_RELEASED;
                        else fingerState = FINGER_UNDEFINED;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (fingerState == FINGER_TOUCHED || fingerState == FINGER_DRAGGING)
                            fingerState = FINGER_DRAGGING;
                        else fingerState = FINGER_UNDEFINED;
                        break;

                    default:
                        fingerState = FINGER_UNDEFINED;

                }

                return false;
            }
        });
        optionIcon.setImageResource(getUncheckedDrawableForOption());
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked != checked) {
            isChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }

    public void setText(CharSequence text) {
        WebSettings webSettings = optionText.getSettings();
        webSettings.setJavaScriptEnabled(true);
        optionText.setBackgroundColor(0);
//        holder.enhancedText.setBackgroundResource(R.color.default_option_color);

        final String html = AppConstants.PRE_BODY + text + AppConstants.POST_BODY;

        final String mimeType = "text/html";
        final String encoding = "UTF-8";
        optionText.loadDataWithBaseURL("", html, mimeType, encoding, "");
    }


    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    public void refreshDrawableState() {
        if (isChecked) {
            optionIcon.setImageResource(getCheckedDrawableForOption());
        } else {
            optionIcon.setImageResource(getUncheckedDrawableForOption());
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ownState = (SavedState) state;
        super.onRestoreInstanceState(ownState.getSuperState());
        setChecked(ownState.checked);
        requestLayout();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ownState = new SavedState(superState);
        ownState.checked = isChecked;
        return ownState;
    }

    @DrawableRes
    public abstract int getCheckedDrawableForOption();

    @DrawableRes
    public abstract int getUncheckedDrawableForOption();


    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean checked;

        private SavedState(Parcel source) {
            super(source);
            checked = source.readInt() == 1;
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(checked ? 1 : 0);
        }
    }
}
