package org.stepic.droid.base;

import android.support.v4.app.Fragment;

import butterknife.ButterKnife;

public class StepicBaseFragment extends Fragment {

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
