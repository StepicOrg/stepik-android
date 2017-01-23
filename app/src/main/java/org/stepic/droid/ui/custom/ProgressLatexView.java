package org.stepic.droid.ui.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.stepic.droid.R;

public class ProgressLatexView extends FrameLayout {

    private LatexSupportableEnhancedFrameLayout optionText;
    private boolean isSet;

    public ProgressLatexView(Context context) {
        this(context, null);
    }

    public ProgressLatexView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public ProgressLatexView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.progressable_latex_supportable_frame_layout, this, true);
        optionText = (LatexSupportableEnhancedFrameLayout) findViewById(R.id.latex_text);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.load_progressbar);

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        WebViewClient client = new WebViewClient() {
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
        };
        optionText.getWebView().setWebViewClient(client);

    }

    public void setPlainOrLaTeXText(String text) {
        if (!isSet) {
            isSet = true;
            optionText.setPlainOrLaTeXText(text);
        }
    }

    public void setAnyText(String text) {
        if (!isSet) {
            isSet = true;
            optionText.setText(text);
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ownState = (SavedState) state;
        super.onRestoreInstanceState(ownState.getSuperState());
        isSet = ownState.isSet;
        requestLayout();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ownState = new SavedState(superState);
        ownState.isSet = isSet;
        return ownState;
    }

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
        boolean isSet;

        private SavedState(Parcel source) {
            super(source);
            isSet = source.readInt() == 1;
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isSet ? 1 : 0);
        }
    }


}
