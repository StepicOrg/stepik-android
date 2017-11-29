package org.stepic.droid.ui.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.view.View;

import timber.log.Timber;

public class CoursesSnapHelper extends SnapHelper {

    private static final int MAX_SCROLL_ON_FLING_DURATION = 100; // ms


    private static final float MILLISECONDS_PER_INCH = 100f;
    private final int rowCount;
    // Orientation helpers are lazily created per LayoutManager.
    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;
    private RecyclerView recyclerView;

    public CoursesSnapHelper(int rowCount) {
        this.rowCount = rowCount;
    }

    @Override
    public void attachToRecyclerView(RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        Timber.d("attach");
    }

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        out[1] = 0;

        Timber.d("calculate distance to final snap " + out[0] + ";" + out[1]);
        return out;
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        Timber.d("findSnapView");
        return getStartView(layoutManager, getHorizontalHelper(layoutManager), true);
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        Timber.d("findTargetSnapPosition result start");
        int result = findTagetSnapPositionInternal(layoutManager, velocityX, velocityY);
        Timber.d("findTargetSnapPosition result " + result);
        return result;
    }

    private int findTagetSnapPositionInternal(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        View startMostChildView = null;
        boolean forwardDirection = velocityX > 0;
        if (layoutManager.canScrollHorizontally()) {
            startMostChildView = getStartView(layoutManager, getHorizontalHelper(layoutManager), forwardDirection);
        }

        if (startMostChildView == null) {
            return RecyclerView.NO_POSITION;
        }
        final int startPosition = layoutManager.getPosition(startMostChildView);
        if (startPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }
        return startPosition;
    }

    @Override
    protected LinearSmoothScroller createSnapScroller(RecyclerView.LayoutManager layoutManager) {
        Timber.d("createSnapScroller");
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return null;
        }
        return new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                int[] snapDistances = calculateDistanceToFinalSnap(recyclerView.getLayoutManager(),
                        targetView);
                final int dx = snapDistances[0];
                final int dy = snapDistances[1];
                final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                Timber.d("onTargetFound dx = " + dx + " dy = " + dy + " time = " + time);
                if (time > 0) {
                    Timber.d("onTargetFound update with time =" + time);
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

            @Override
            protected int calculateTimeForScrolling(int dx) {
                return Math.min(MAX_SCROLL_ON_FLING_DURATION, super.calculateTimeForScrolling(dx));
            }
        };
    }


    private int distanceToStart(View targetView, OrientationHelper helper) {
        return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
    }

    private View getStartView(
            RecyclerView.LayoutManager layoutManager,
            OrientationHelper helper,
            boolean forwardDirection) {
        GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
        int firstChildPosition = gridLayoutManager.findFirstVisibleItemPosition();

        boolean isLastItem = gridLayoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1;

        if (firstChildPosition == RecyclerView.NO_POSITION || isLastItem) {
            return null;
        }

        View child = layoutManager.findViewByPosition(firstChildPosition);

        int endOfChild = helper.getDecoratedEnd(child);
        int threshold = (int) (helper.getDecoratedMeasurement(child) * 1);
        Timber.d("get start view first visible = " + firstChildPosition);
        Timber.d("forwardDirection = " + forwardDirection);
        Timber.d("endOfChild = " + endOfChild + " threshold = " + threshold);
        if (endOfChild >= threshold && endOfChild > 0) {
            //here we can change "scrollable" of the list. for example divide it to 3 or 4
            return child;
        } else if (forwardDirection) {
            return getNextView(firstChildPosition, layoutManager);
        } else {
            return getPreviousView(firstChildPosition, layoutManager);
        }
    }

    private View getNextView(int currentPosition, RecyclerView.LayoutManager layoutManager) {
        int lastPosition = layoutManager.getItemCount() - 1;
        int currentPositionPlusRowCount = currentPosition + rowCount;
        int nextPosition = Math.min(lastPosition, currentPositionPlusRowCount);
        return layoutManager.findViewByPosition(nextPosition);
    }

    private View getPreviousView(int currentPosition, RecyclerView.LayoutManager layoutManager) {
        int currentPositionMinusRowCount = currentPosition - rowCount;
        int previousPosition = Math.max(0, currentPositionMinusRowCount);
        return layoutManager.findViewByPosition(previousPosition);
    }

    @Nullable
    private View findStartView(RecyclerView.LayoutManager layoutManager) {
        return layoutManager.getChildAt(0);
    }


    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }
}
