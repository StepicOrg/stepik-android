package org.stepic.droid.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.model.Step;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.HtmlHelper;

import butterknife.Bind;

public abstract class FragmentStepBase extends FragmentBase {

    @Bind(R.id.text_header)
    protected TextView mHeaderTv;

    protected Step mStep;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStep = (Step) getArguments().getSerializable(AppConstants.KEY_STEP_BUNDLE);
        if (mStep != null &&
                mStep.getBlock() != null &&
                mStep.getBlock().getText() != null &&
                mStep.getBlock().getText() != "") {
            mHeaderTv.setText(HtmlHelper.fromHtml(mStep.getBlock().getText()));
            mHeaderTv.setVisibility(View.VISIBLE);
        } else {
            mHeaderTv.setVisibility(View.GONE);
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
