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

import org.jetbrains.annotations.NotNull;

public class CoursesSnapHelper extends SnapHelper {

    private static final int MAX_DURATION_OF_SCROLL = 100;
    private static final float MILLISECONDS_PER_INCH = 100f;
    private final int rowCount;

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
    }

    @NotNull
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        int[] out = new int[2];
        out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager));
        out[1] = 0;
        return out;
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        return getStartView(layoutManager, getHorizontalHelper(layoutManager), true);
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        View startMostChildView;
        boolean forwardDirection = velocityX > 0;
        startMostChildView = getStartView(layoutManager, getHorizontalHelper(layoutManager), forwardDirection);

        if (startMostChildView == null) {
            return RecyclerView.NO_POSITION;
        }

        return layoutManager.getPosition(startMostChildView);
    }

    @NotNull
    @Override
    protected RecyclerView.SmoothScroller createScroller(RecyclerView.LayoutManager layoutManager) {
        return new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                int[] snapDistances = calculateDistanceToFinalSnap(recyclerView.getLayoutManager(),
                        targetView);
                final int dx = snapDistances[0];
                final int dy = snapDistances[1];
                final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

            @Override
            protected int calculateTimeForScrolling(int dx) {
                return Math.min(MAX_DURATION_OF_SCROLL, super.calculateTimeForScrolling(dx));
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
        int threshold = helper.getDecoratedMeasurement(child);
        if (endOfChild >= threshold && endOfChild > 0) {
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
        int previousPosition = Math.max(0, currentPosition);
        return layoutManager.findViewByPosition(previousPosition);
    }

    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }
}
