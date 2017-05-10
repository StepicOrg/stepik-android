package org.stepic.droid.ui.custom_exo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

public class AppCompatSeekTimeBar extends AppCompatSeekBar implements TimeBar {

    private TimeBar.OnScrubListener listener;

    public AppCompatSeekTimeBar(Context context) {
        super(context);
    }

    public AppCompatSeekTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppCompatSeekTimeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setListener(OnScrubListener listener) {
        this.listener = listener;
    }

    @Override
    public void setKeyTimeIncrement(long time) {

    }

    @Override
    public void setKeyCountIncrement(int count) {

    }

    @Override
    public void setPosition(long position) {
        setProgress((int) position);
    }

    @Override
    public void setBufferedPosition(long bufferedPosition) {
        setSecondaryProgress((int) bufferedPosition);
    }

    @Override
    public void setDuration(long duration) {
        setMax((int) duration);
    }

    @Override
    public void setAdBreakTimesMs(@Nullable long[] adBreakTimesMs, int adBreakCount) {
        //// TODO: 10.05.17 implement it, when needed
    }
}
