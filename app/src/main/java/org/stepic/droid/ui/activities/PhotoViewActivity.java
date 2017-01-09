package org.stepic.droid.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.SingleFragmentActivity;
import org.stepic.droid.ui.fragments.PhotoViewFragment;

public class PhotoViewActivity extends SingleFragmentActivity {

    public static final String pathKey = "pathKey";
//    private GestureDetector gestureDetector;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        gestureDetector = new GestureDetector(this, new SwipeDetector());
    }

//    private class SwipeDetector extends GestureDetector.SimpleOnGestureListener {
//        private static final int SWIPE_MIN_DISTANCE = 120;
//        private static final int SWIPE_MAX_OFF_PATH = 250;
//        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
//
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//
//            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
//                return false;
//
//            if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                finish();
//                return true;
//            }
//
//            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                finish();
//                overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_top);
//                return true;
//            }
//
//            return false;
//        }
//    }
//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        // TouchEvent dispatcher.
//        if (gestureDetector != null) {
//            if (gestureDetector.onTouchEvent(ev))
//                // If the gestureDetector handles the event, a swipe has been
//                // executed and no more needs to be done.
//                return true;
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return gestureDetector.onTouchEvent(event);
//    }
}
