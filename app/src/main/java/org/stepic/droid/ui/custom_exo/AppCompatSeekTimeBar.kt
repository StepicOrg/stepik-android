package org.stepic.droid.ui.custom_exo

import android.content.Context
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import timber.log.Timber

class AppCompatSeekTimeBar : AppCompatSeekBar, TimeBar, SeekBar.OnSeekBarChangeListener {
    private var listener: TimeBar.OnScrubListener? = null
    private val notTouchableOnTouchListener: (View?, MotionEvent?) -> Boolean by lazy {
        { _: View?, _: MotionEvent? -> true }
    }

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setOnSeekBarChangeListener(this)
    }

    override fun setListener(listener: TimeBar.OnScrubListener?) {
        this.listener = listener
    }

    override fun setKeyTimeIncrement(time: Long) {
        Timber.d("setKeyTimeIncrement($time)")
    }

    override fun setKeyCountIncrement(count: Int) {
        Timber.d("setKeyCountIncrement($count)")
    }

    override fun setPosition(position: Long) {
        progress = position.toInt()
    }

    override fun setBufferedPosition(bufferedPosition: Long) {
        secondaryProgress = bufferedPosition.toInt()
    }

    override fun setDuration(duration: Long) {
        max = duration.toInt()
    }

    override fun setAdBreakTimesMs(adBreakTimesMs: LongArray?, adBreakCount: Int) {
        //// TODO: 10.05.17 implement it, when needed
    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            listener?.onScrubMove(this, progress.toLong())
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        listener?.onScrubStart(this)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        listener?.onScrubStop(this, this.progress.toLong(), false)
    }

    override fun setEnabled(enabled: Boolean) {
        //we do it, because setEnabled add some extra effects, we should make it just not seekable
        if (!enabled) {
            setOnTouchListener(notTouchableOnTouchListener)
        } else {
            setOnTouchListener(null)
        }
    }


}
