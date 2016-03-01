package org.stepic.droid.view.fragments

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.io.File
import java.lang.ref.WeakReference
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
    private val mPlayerListener = MyPlayerListener(this)

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
            mMediaPlayer?.setEventListener(mPlayerListener)

            // Set up video output
            val vout = mMediaPlayer!!.getVLCVout()
            vout.setVideoView(mVideoView)
            vout.addCallback(this)
            vout.attachViews()

            val file = File (mFilePath)
            var uri: Uri?
            if (file.exists()) {
                uri = Uri.fromFile(file)
            } else {
                uri = Uri.parse(mFilePath)
            }
            val m = Media(libvlc, uri)
            mMediaPlayer?.setMedia(m)

            mMediaPlayer?.setRate(1.5f)
            mMediaPlayer?.play()
        } catch (e: Exception) {
            Toast.makeText(activity, "Error creating player!", Toast.LENGTH_LONG).show()
        }

    }

    private fun releasePlayer() {
        if (libvlc == null)
            return
        mMediaPlayer!!.stop()
        val vout = mMediaPlayer!!.getVLCVout()
        vout.removeCallback(this)
        vout.detachViews()
        mVideoViewHolder = null
        libvlc?.release()
        libvlc = null

        mVideoWidth = 0
        mVideoHeight = 0
    }

    override fun eventHardwareAccelerationError() {
        throw UnsupportedOperationException()
    }

    private class MyPlayerListener(owner: VideoFragment) : MediaPlayer.EventListener {
        private val mOwner: WeakReference<VideoFragment>

        init {
            mOwner = WeakReference<VideoFragment>(owner)
        }

        override fun onEvent(event: MediaPlayer.Event) {
            val player = mOwner.get()

            when (event.type) {
                MediaPlayer.Event.EndReached -> {
                    Log.d("lala", "MediaPlayerEndReached")
                    player.releasePlayer()
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        createPlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
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


}