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

public class StepFragmentBase extends FragmentBase {

    @Bind(R.id.text_header)
    TextView mHeaderTv;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Step step = (Step) getArguments().getSerializable(AppConstants.KEY_STEP_BUNDLE);
        if (step != null &&
                step.getBlock() != null &&
                step.getBlock().getText() != null &&
                step.getBlock().getText() != "") {
            mHeaderTv.setText(HtmlHelper.fromHtml(step.getBlock().getText()));
            mHeaderTv.setVisibility(View.VISIBLE);
        } else {
            mHeaderTv.setVisibility(View.GONE);
        }
    }
}
