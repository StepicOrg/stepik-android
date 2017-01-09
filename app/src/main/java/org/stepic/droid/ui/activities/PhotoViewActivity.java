package org.stepic.droid.ui.activities;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.SingleFragmentActivity;
import org.stepic.droid.ui.fragments.PhotoViewFragment;

public class PhotoViewActivity extends SingleFragmentActivity {

    public static final String pathKey = "pathKey";

    @Nullable
    @Override
    protected Fragment createFragment() {
        String path = getIntent().getStringExtra(pathKey);
        return PhotoViewFragment.newInstance(path);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
