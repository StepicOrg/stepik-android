package org.stepic.droid.ui.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.stepic.droid.R;

public abstract class StepikOptionView extends FrameLayout implements Checkable {

    private ImageView optionIcon;

    private LatexSupportableEnhancedFrameLayout optionText;

    private ProgressBar progressBar;

    private boolean broadcasting;

    private OnCheckedChangeListener onCheckedChangeListener;
    private OnCheckedChangeListener onCheckedChangeWidgetListener;


    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    private boolean isChecked;
    private FrameLayout rippleEffectFrameLayout;

    public StepikOptionView(Context context) {
        this(context, null);
    }

    public StepikOptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepikOptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.stepic_compound_button, this, true);
        optionIcon = findViewById(R.id.image_compound_button);
        optionText = findViewById(R.id.text_compound_button);
        progressBar = findViewById(R.id.loadProgressbar);
        rippleEffectFrameLayout = findViewById(R.id.rippleFrameLayoutInOption);

        init();
    }

    private void init() {
        rippleEffectFrameLayout.setOnClickListener(v -> performClick());

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        optionText.getWebView().setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(GONE);

            }
        });

        optionIcon.setImageResource(getUncheckedDrawableForOption());
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked != checked) {
            isChecked = checked;
            refreshDrawableState();

            // Avoid infinite recursions if setChecked() is called from a listener
            if (broadcasting) {
                return;
            }

            broadcasting = true;
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(this, isChecked);
            }
            if (onCheckedChangeWidgetListener != null) {
                onCheckedChangeWidgetListener.onCheckedChanged(this, isChecked);
            }

            broadcasting = false;
        }

    }

    @Override
    public void setEnabled(boolean enabled) {
        rippleEffectFrameLayout.setClickable(enabled);
        super.setEnabled(enabled);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }

    void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        onCheckedChangeWidgetListener = listener;
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }

    public void setText(String text) {
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

    public interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        void onCheckedChanged(StepikOptionView buttonView, boolean isChecked);
    }
}
