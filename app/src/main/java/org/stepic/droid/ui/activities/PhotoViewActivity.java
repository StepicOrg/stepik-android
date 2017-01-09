package org.stepic.droid.ui.activities;

import android.support.v4.app.Fragment;

import org.jetbrains.annotations.Nullable;
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
}
