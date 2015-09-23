package org.stepic.droid.base;

import android.support.v4.app.Fragment;

import org.stepic.droid.core.IShell;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class StepicBaseFragment extends Fragment {

    @Inject
    public IShell mShell;

    public StepicBaseFragment () {
        MainApplication.component(MainApplication.getAppContext()).inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
