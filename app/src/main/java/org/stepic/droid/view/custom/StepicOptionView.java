package org.stepic.droid.view.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.stepic.droid.R;
import org.stepic.droid.util.DpPixelsHelper;

public abstract class StepicOptionView extends RelativeLayout implements Checkable {

    private ImageView optionIcon;

    private LatexSupportableWebView optionText;

    private ProgressBar progressBar;


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
        optionText = (LatexSupportableWebView) findViewById(R.id.text_compound_button);
        progressBar = (ProgressBar) findViewById(R.id.load_progressbar);
        init();
    }

    private void init() {
        int dp8 = (int) DpPixelsHelper.convertDpToPixel(8);
        setPadding(dp8, dp8, dp8, dp8);
        int dp48 = (int) DpPixelsHelper.convertDpToPixel(48);
        setMinimumHeight(dp48);

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setClickable(true);

        optionText.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("eee", "onPageStarted");
                progressBar.setVisibility(VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("eee", "onPageFinished");
                progressBar.setVisibility(GONE);

            }
        });
        optionText.setClickable(true);
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

                            StepicOptionView parent = (StepicOptionView) v.getParent(); //// FIXME: 27.04.16 find better way for handling
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
        optionText.setText(text);
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
        if (isEnabled()) {
            toggle();
        }
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
