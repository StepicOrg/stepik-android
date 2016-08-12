package org.stepic.droid.ui.activities;

import org.stepic.droid.base.FragmentActivityBase;

public abstract class BackToExitActivityBase extends FragmentActivityBase {
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down);
    }
}
