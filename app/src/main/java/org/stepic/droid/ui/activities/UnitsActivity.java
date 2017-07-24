package org.stepic.droid.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.SingleFragmentActivity;
import org.stepic.droid.model.Section;
import org.stepic.droid.ui.fragments.UnitsFragment;
import org.stepic.droid.util.AppConstants;

import java.util.List;

public class UnitsActivity extends SingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.units_lessons_title);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        }
    }

    @Nullable
    @Override
    protected Fragment createFragment() {
        Section section = getIntent().getParcelableExtra(AppConstants.KEY_SECTION_BUNDLE);
        return UnitsFragment.newInstance(section);
    }
}
