package org.stepic.droid.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.stepic.droid.R;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.view.custom.LatexSupportableWebView;

import butterknife.Bind;

public abstract class StepBaseFragment extends FragmentBase {

    @Bind(R.id.text_header)
    protected LatexSupportableWebView headerWv;

    protected Step step;
    protected Lesson lesson;
    protected Unit unit;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        step = (Step) getArguments().getSerializable(AppConstants.KEY_STEP_BUNDLE);
        lesson = (Lesson) getArguments().getSerializable(AppConstants.KEY_LESSON_BUNDLE);
        unit = (Unit) getArguments().getSerializable(AppConstants.KEY_UNIT_BUNDLE);

        if (step != null &&
                step.getBlock() != null &&
                step.getBlock().getText() != null &&
                !step.getBlock().getText().equals("")) {

            headerWv.setText(step.getBlock().getText());
            headerWv.setVisibility(View.VISIBLE);
        } else {
            headerWv.setVisibility(View.GONE);
        }
        bus.register(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        hideSoftKeypad();
    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        super.onDestroyView();
    }
}
