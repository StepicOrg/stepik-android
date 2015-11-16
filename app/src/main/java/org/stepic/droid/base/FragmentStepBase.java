package org.stepic.droid.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.stepic.droid.R;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.util.AppConstants;

import butterknife.Bind;

public abstract class FragmentStepBase extends FragmentBase {

    @Bind(R.id.text_header)
    protected WebView mHeaderWv;

    protected Step mStep;
    protected Lesson mLesson;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStep = (Step) getArguments().getSerializable(AppConstants.KEY_STEP_BUNDLE);
        mLesson = (Lesson) getArguments().getSerializable(AppConstants.KEY_LESSON_BUNDLE);

        if (mStep != null &&
                mStep.getBlock() != null &&
                mStep.getBlock().getText() != null &&
                mStep.getBlock().getText() != "") {
            WebSettings webSettings = mHeaderWv.getSettings();
            webSettings.setJavaScriptEnabled(true);

            final String html = AppConstants.PRE_BODY + mStep.getBlock().getText() + AppConstants.POST_BODY;

            final String mimeType = "text/html";
            final String encoding = "UTF-8";
            mHeaderWv.loadDataWithBaseURL("", html, mimeType, encoding, "");
//            mHeaderWv.setText(HtmlHelper.fromHtml(mStep.getBlock().getText()));
            mHeaderWv.setVisibility(View.VISIBLE);
        } else {
            mHeaderWv.setVisibility(View.GONE);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
    }
}
