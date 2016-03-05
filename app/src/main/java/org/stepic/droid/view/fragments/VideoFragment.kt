package org.stepic.droid.view.fragments

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.AppCompatSeekBar
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.util.TimeUtil
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.io.File
import java.util.*

class VideoFragment : FragmentBase(), LibVLC.HardwareAccelerationError, IVLCVout.Callback {
    companion object {
        private val VIDEO_KEY = "video_key"
        fun newInstance(videoUri: String): VideoFragment {
            val args = Bundle()
            args.putString(VIDEO_KEY, videoUri)
            val fragment = VideoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var mVideoView: SurfaceView? = null;
    var mFilePath: String? = null;
    var mVideoViewHolder: SurfaceHolder? = null
    var libvlc: LibVLC? = null
    var mMediaPlayer: MediaPlayer? = null
    var mVideoWidth: Int = 0
    var mVideoHeight: Int = 0
    private var mPlayerListener : MyPlayerListener? = null

    var isSeekBarDragging: Boolean = false

    //Controller:
    var mPlayerSeekBar: AppCompatSeekBar? = null
    var mCurrentTime: TextView? = null
    var mMaxTime: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFilePath = arguments.getString(VIDEO_KEY)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_video, container, false);
        mVideoView = v?.findViewById(R.id.texture_video_view) as? SurfaceView
        mVideoViewHolder = mVideoView?.holder
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupController()
    }

    private fun createPlayer() {
        releasePlayer()
        try {
            val options = ArrayList<String>()
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles")
            options.add("--audio-time-stretch") // time stretching
            options.add("-vvv") // verbosity
            libvlc = LibVLC(options)
            libvlc?.setOnHardwareAccelerationError(this)
            mVideoViewHolder?.setKeepScreenOn(true)

            // Create media player
            mMediaPlayer = MediaPlayer(libvlc)
            mPlayerListener = MyPlayerListener(this)
            mMediaPlayer?.setEventListener(mPlayerListener)

            // Set up video output
            val vout = mMediaPlayer?.getVLCVout()
            vout?.setVideoView(mVideoView)
            vout?.addCallback(this)
            vout?.attachViews()

            val file = File (mFilePath)
            var uri: Uri?
            if (file.exists()) {
                uri = Uri.fromFile(file)
            } else {
                uri = Uri.parse(mFilePath)
            }
            val m = Media(libvlc, uri)
            mMediaPlayer?.setMedia(m)

            //            mMediaPlayer?.setRate(1.5f)
            mMediaPlayer?.play()
        } catch (e: Exception) {
            Toast.makeText(activity , "Error creating player!", Toast.LENGTH_LONG).show()
        }

    }

    private fun releasePlayer() {
        mMediaPlayer?.stop()
        val vout = mMediaPlayer?.getVLCVout()
        vout?.removeCallback(this)
        vout?.detachViews()
        mPlayerListener = null
//        mVideoViewHolder = null
        mMediaPlayer?.setEventListener(null)
        libvlc?.release()
        libvlc?.setOnHardwareAccelerationError (null)
        libvlc = null
        mMediaPlayer?.release()
        mMediaPlayer = null
        mVideoWidth = 0
        mVideoHeight = 0
    }

    override fun eventHardwareAccelerationError() {

        throw UnsupportedOperationException()
        //fixme: recreate player
    }


    override fun onResume() {
        super.onResume()
        createPlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroyView() {
        destroyVideoView()
        destroyController()
        super.onDestroyView()
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    override fun onSurfacesCreated(p0: IVLCVout?) {
    }

    override fun onSurfacesDestroyed(p0: IVLCVout?) {
    }

    override fun onNewLayout(vout: IVLCVout, width: Int, height: Int, visibleWidth: Int, visibleHeight: Int, sarNum: Int, sarDen: Int) {
        if (width * height == 0)
            return

        // store video size
        mVideoWidth = width
        mVideoHeight = height
        setSize(mVideoWidth, mVideoHeight)
    }

    private fun setSize(width: Int, height: Int) {
        mVideoWidth = width
        mVideoHeight = height
        if (mVideoWidth * mVideoHeight <= 1)
            return

        if (mVideoViewHolder == null || mVideoView == null)
            return

        // get screen size
        var w = activity.getWindow().getDecorView().getWidth()
        var h = activity.getWindow().getDecorView().getHeight()

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        if (w > h && isPortrait || w < h && !isPortrait) {
            val i = w
            w = h
            h = i
        }

        val videoAR = mVideoWidth.toFloat() / mVideoHeight.toFloat()
        val screenAR = w.toFloat() / h.toFloat()

        if (screenAR < videoAR)
            h = (w / videoAR).toInt()
        else
            w = (h * videoAR).toInt()

        // force surface buffer size
        mVideoViewHolder?.setFixedSize(mVideoWidth, mVideoHeight)

        // set display size
        val lp = mVideoView?.getLayoutParams()
        lp?.width = w
        lp?.height = h
        mVideoView?.setLayoutParams(lp)
        mVideoView?.invalidate()
    }

    private fun setupController() {
        view?.let {
            val container = it.findViewById(R.id.video_view_container) as ViewGroup
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val controller = inflater.inflate(R.layout.player_controller, null)

            initSeekBar(controller)
            mCurrentTime = controller.findViewById(R.id.current_video_time) as? TextView
            mMaxTime = controller.findViewById(R.id.overall_video_time) as? TextView
            container.addView(controller)
        }
    }

    private fun  destroyVideoView(){
        mVideoView = null
        mVideoViewHolder = null
    }

    private fun destroyController() {
        //todo: implement other
        mPlayerSeekBar = null
        mCurrentTime = null
        mMaxTime = null
    }

    private fun initSeekBar(view: View) {
        mPlayerSeekBar = view.findViewById(R.id.player_controller_progress) as? AppCompatSeekBar
        mPlayerSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (mMediaPlayer?.isReleased?:true){
                        createPlayer()
                    }
                    mMediaPlayer?.position = progress.toFloat() / seekBar.max.toFloat()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeekBarDragging = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isSeekBarDragging = false
            }
        })
    }

    private class MyPlayerListener(owner: VideoFragment) : MediaPlayer.EventListener {
        private var mOwner: VideoFragment?

        init {
            mOwner = owner
        }

        override fun onEvent(event: MediaPlayer.Event) {
            val player = mOwner?.mMediaPlayer
            when (event.type) {
                MediaPlayer.Event.Playing -> {
                    if (player?.isPlaying ?: false) {
                        player?.length?.let {
                            Log.d("lala", "length = " + it)
                            mOwner?.mMaxTime?.text = TimeUtil.getFormattedVideoTime(it)
                        }
                    }
                }
                MediaPlayer.Event.EndReached -> {
                    Log.d("lala", "MediaPlayerEndReached")
                    player?.length?.let {
                        mOwner?.mCurrentTime?.text = TimeUtil.getFormattedVideoTime(it)
                    }
                    mOwner?.releasePlayer()
                }
                MediaPlayer.Event.PositionChanged -> {
                    val currentPos = player?.position
                    currentPos?.let {
                        Log.d("lala", "positionChanged " + it)
                        mOwner?.mPlayerSeekBar?.let {
                            if (!(mOwner?.isSeekBarDragging?:false)) {
                                val max = it.max
                                it.progress = (max.toFloat() * currentPos).toInt()
                            }
                        }
                    }
                }
                MediaPlayer.Event.TimeChanged -> {
                    Log.d("lala", "timechanged " + player?.time)
                    player?.time?.let {
                        mOwner?.mCurrentTime?.text =
                                TimeUtil.getFormattedVideoTime(it)
                    }
                }
            }
        }
    }

}