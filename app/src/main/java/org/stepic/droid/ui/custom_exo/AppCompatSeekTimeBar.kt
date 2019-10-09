package org.stepic.droid.ui.custom_exo

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import com.google.android.exoplayer2.ui.TimeBar
import timber.log.Timber

class AppCompatSeekTimeBar
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): AppCompatSeekBar(context, attrs, defStyleAttr), TimeBar, SeekBar.OnSeekBarChangeListener {
    private val listeners: MutableList<TimeBar.OnScrubListener> = mutableListOf()
    private val notTouchableOnTouchListener: (View?, MotionEvent?) -> Boolean by lazy {
        { _: View?, _: MotionEvent? -> true }
    }

    init {
        setOnSeekBarChangeListener(this)
    }

    override fun addListener(listener: TimeBar.OnScrubListener?) {
        listener?.let(listeners::add)
    }

    override fun removeListener(listener: TimeBar.OnScrubListener?) {
        listener?.let(listeners::remove)
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

    override fun setAdGroupTimesMs(adGroupTimesMs: LongArray?, playedAdGroups: BooleanArray?, adGroupCount: Int) {
        // TODO: 10.05.17 implement it, when needed
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            listeners.forEach {
                it.onScrubMove(this, progress.toLong())
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        listeners.forEach {
            it.onScrubStart(this, progress.toLong())
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        listeners.forEach {
            it.onScrubStop(this, progress.toLong(), false)
        }
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
