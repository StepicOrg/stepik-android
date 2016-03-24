package org.stepic.droid.view.fragments

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.AppCompatSeekBar
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.*
import android.widget.*
import com.squareup.otto.Subscribe
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.base.MainApplication
import org.stepic.droid.core.MyStatePhoneListener
import org.stepic.droid.events.IncomingCallEvent
import org.stepic.droid.events.audio.AudioFocusLossEvent
import org.stepic.droid.preferences.VideoPlaybackRate
import org.stepic.droid.util.AndroidDevices
import org.stepic.droid.util.TimeUtil
import org.stepic.droid.view.custom.TouchDispatchableFrameLayout
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.AndroidUtil
import java.io.File
import java.util.*

class VideoFragment : FragmentBase(), LibVLC.HardwareAccelerationError, IVLCVout.Callback {
    companion object {
        private val TIMEOUT_BEFORE_HIDE = 4500L
        private val INDEX_PLAY_IMAGE = 0
        private val INDEX_PAUSE_IMAGE = 1
        private val JUMP_TIME_MILLIS = 10000L
        private val JUMP_MAX_DELTA = 3000L
        private val VIDEO_KEY = "video_key"
        private val DELTA_TIME = 0L
        private val TAG = "video player: "
        fun newInstance(videoUri: String): VideoFragment {
            val args = Bundle()
            args.putString(VIDEO_KEY, videoUri)
            val fragment = VideoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    val myStatePhoneListener = MyStatePhoneListener()
    val tmgr = MainApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    var mSurfaceFrame: FrameLayout? = null
    var mFragmentContainer: ViewGroup? = null
    var mVideoView: SurfaceView? = null;
    var mFilePath: String? = null;
    var libvlc: LibVLC? = null
    var mMediaPlayer: MediaPlayer? = null
    var mVideoWidth: Int = 0
    var mVideoHeight: Int = 0
    private val mPlayerListener: MyPlayerListener = MyPlayerListener(this)
    var mMaxTimeInMillis: Long? = null
    var mCurrentTimeInMillis: Long = 0L
    var mProgressBar: ProgressBar? = null

    var isSeekBarDragging: Boolean = false

    //Controller:
    var mController: TouchDispatchableFrameLayout? = null
    var mPlayerSeekBar: AppCompatSeekBar? = null
    var mCurrentTime: TextView? = null
    var mMaxTime: TextView? = null
    var mPlayPauseSwitcher: ImageSwitcher? = null
    var mPlayImageView: ImageView? = null
    var mPauseImageView: ImageView? = null
    var mJumpForwardImageView: ImageView? = null
    var mJumpBackwardImageView: ImageView? = null
    var mVideoRateChooser: ImageView? = null
    private var mSlashTime: TextView? = null
    var isControllerVisible = true

    var isEndReached = false

    var isOnStartAfterSurfaceDestroyed = false

    private var mVideoVisibleHeight: Int = 0
    private var mVideoVisibleWidth: Int = 0
    private var mSarNum: Int = 0
    private var mSarDen: Int = 0
    private var isOnResumeDirectlyAfterOnCreate = true

    private var isLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        YandexMetrica.reportEvent(TAG + "onCreate")
        retainInstance = true
        mFilePath = arguments.getString(VIDEO_KEY)
        //        createPlayer()
        initPhoneStateListener()
        isOnResumeDirectlyAfterOnCreate = true
        //        playPlayer()
    }


    private val mReceiver: BroadcastReceiver = MyBroadcastReceiver(this)

    private class MyBroadcastReceiver(owner: VideoFragment) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getAction()) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY ->
                    mOwner?.pausePlayer()
            }

        }

        private var mOwner: VideoFragment?

        init {
            mOwner = owner
        }
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        YandexMetrica.reportEvent(TAG + "onCreateView")
        mFragmentContainer = inflater?.inflate(R.layout.fragment_video, container, false) as ViewGroup

        mProgressBar = mFragmentContainer?.findViewById(R.id.load_progressbar) as ProgressBar
        mProgressBar?.visibility = View.VISIBLE

        mSurfaceFrame = mFragmentContainer?.findViewById(R.id.player_surface_frame) as FrameLayout
        mVideoView = mFragmentContainer?.findViewById(R.id.texture_video_view) as SurfaceView
        mFragmentContainer?.setOnTouchListener { view, motionEvent ->
            if (!isLoading) {
                Log.d("ttt", "mFragmentContainer?.setOnTouchListener  " + view.javaClass.canonicalName)
                showController(!isControllerVisible)
            }
            false
        }
        setupController(mFragmentContainer)
        isOnStartAfterSurfaceDestroyed = false
        Log.d("ttt", "onCreateView")

        var filter = IntentFilter()
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        activity.registerReceiver(mReceiver, filter)
        startLoading()
        return mFragmentContainer
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        YandexMetrica.reportEvent(TAG + "onActivityCreated")
        hideNavigationBar(false)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    private fun bindViewWithPlayer() {
        YandexMetrica.reportEvent(TAG + "bindViewWithPlayer")
        val vout = mMediaPlayer?.getVLCVout()
        vout?.setVideoView(mVideoView)
        vout?.addCallback(this)
        vout?.attachViews()
        //
        mMediaPlayer?.setEventListener(mPlayerListener)

        mPlayPauseSwitcher?.setClickable(true)

    }

    private fun createPlayer() {
        try {
            YandexMetrica.reportEvent(TAG + "createPlayer")
            val options = ArrayList<String>()

            options.add("--audio-time-stretch") // time stretching
            options.add("--no-drop-late-frames") //help when user accelerates video
            options.add("--no-skip-frames")
            options.add("-vvv") // verbosity

            //            //Magic commands:  HW_ACCELERATION_FULL
            //            val decoder = HWDecoderUtil.getDecoderFromDevice()
            //            if (decoder == HWDecoderUtil.Decoder.NONE) {
            //                options.add("--codec=all")
            //            } else {
            //
            //                /*
            //         * Set higher caching values if using iomx decoding, since some omx
            //         * decoders have a very high latency, and if the preroll data isn't
            //         * enough to make the decoder output a frame, the playback timing gets
            //         * started too soon, and every decoded frame appears to be too late.
            //         * On Nexus One, the decoder latency seems to be 25 input packets
            //         * for 320x170 H.264, a few packets less on higher resolutions.
            //         * On Nexus S, the decoder latency seems to be about 7 packets.
            //         */
            //                options.add("--file-caching=1500")
            //                options.add("--network-caching=1500")
            //
            //                val sb = StringBuilder("--codec=")
            //                if (decoder == HWDecoderUtil.Decoder.MEDIACODEC)
            //                    sb.append(if (AndroidUtil.isLolliPopOrLater()) "mediacodec_ndk" else "mediacodec_jni").append(",")
            //                else if (decoder == HWDecoderUtil.Decoder.OMX)
            //                    sb.append("iomx,")
            //                else
            //                    sb.append(if (AndroidUtil.isLolliPopOrLater()) "mediacodec_ndk" else "mediacodec_jni").append(",iomx,")
            //                sb.append("all")
            //
            //                options.add(sb.toString())
            //            }
            //            //end magic

            libvlc = LibVLC(options)
            libvlc?.setOnHardwareAccelerationError(this)

            // Create media player
            mMediaPlayer = MediaPlayer(libvlc)
            //            mPlayerListener = MyPlayerListener(this)
            //            mMediaPlayer?.setEventListener(mPlayerListener)

            // Set up video output
            //            val vout = mMediaPlayer?.getVLCVout()
            //            vout?.setVideoView(mVideoView)
            //            vout?.addCallback(this)
            //            vout?.attachViews()

            val file = File (mFilePath)
            var uri: Uri?
            if (file.exists()) {
                uri = Uri.fromFile(file)
            } else {
                uri = Uri.parse(mFilePath)
            }

            val media = Media(libvlc, uri)
            //            mMedia?.setHWDecoderEnabled(true, true)
            mMediaPlayer?.setMedia(media)
            media.release()

            mMediaPlayer?.setRate(mUserPreferences.videoPlaybackRate.rateFloat)
            isEndReached = false
        } catch (e: Exception) {
            YandexMetrica.reportEvent(TAG + "Error creating player")
            Toast.makeText(activity, "Error creating player!", Toast.LENGTH_LONG).show()
        }

    }

    private fun releasePlayer() {
        YandexMetrica.reportEvent(TAG + "releasePlayer")
        mMediaPlayer?.stop()
        val vout = mMediaPlayer?.getVLCVout()
        vout?.removeCallback(this)
        vout?.detachViews()
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
        //throw UnsupportedOperationException()
        YandexMetrica.reportEvent(TAG + "vlc error hardware")
        recreateAndPreloadPlayer()
    }

    override fun onStart() {
        super.onStart()
        YandexMetrica.reportEvent(TAG + "onStart")
        Log.d("ttt", "onStart")
    }


    override fun onResume() {
        super.onResume()
        Log.d("tttt", "onResume")
        bus.register(this)
        if (!isLoading) {
            showController(true)
        }
        YandexMetrica.reportEvent(TAG + "onResume")
        recreateAndPreloadPlayer()
    }

    fun recreateAndPreloadPlayer() {
        YandexMetrica.reportEvent(TAG + "recreateAndPreloadPlayer")
        val km = MainApplication.getAppContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        createPlayer()
        bindViewWithPlayer()
        if (isOnResumeDirectlyAfterOnCreate) {
            isOnResumeDirectlyAfterOnCreate = false
            mMediaPlayer?.setEventListener(mPlayerListener)

        } else {
            mMediaPlayer?.setEventListener(preRollListener)

        }
        if (!km.inKeyguardRestrictedInputMode()) {
            mMediaPlayer?.play()
            mMediaPlayer?.time = mCurrentTimeInMillis
            mPlayerSeekBar?.let {
                if (!isSeekBarDragging) {
                    val max = it.max
                    var positionByHand = 0f
                    if (mMaxTimeInMillis != null) {
                        val maxTime = mMaxTimeInMillis ?: 1L
                        positionByHand = (mCurrentTimeInMillis.toFloat() / maxTime.toFloat()).toFloat()

                    }
                    if (positionByHand > max) {
                        positionByHand = 0f
                    }

                    it.progress = (max.toFloat() * positionByHand).toInt()
                }
            }
        }
        YandexMetrica.reportEvent(TAG + "recreateAndPreloadPlayer end")

    }

    override fun onPause() {
        super.onPause()
        showPlay() // because callback not working here
        val player = mMediaPlayer
        if (player == null || player.isReleased) {
            mCurrentTimeInMillis = 0L
        } else if (player.time >= 0) {
            Log.d("ttt", "OnPause mplayer time " + mMediaPlayer?.time)
            mCurrentTimeInMillis = (mMediaPlayer?.time ?: 0L) - DELTA_TIME
        }
        if (mCurrentTimeInMillis < 0L) mCurrentTimeInMillis = 0L
        pausePlayer()
        mMediaPlayer?.setEventListener(null)
        releasePlayer()
        YandexMetrica.reportEvent(TAG + "onPause end")
        Log.d("ttt", "onPause")
        clearAutoHideQueue()
        mAudioFocusHelper.releaseAudioFocus()
        bus.unregister(this)
    }

    override fun onStop() {
        super.onStop()
        Log.d("ttt", "onStop")
        YandexMetrica.reportEvent(TAG + "onStop end")
    }

    override fun onDestroyView() {
        destroyVideoView()
        destroyController()
        activity?.unregisterReceiver(mReceiver)
        super.onDestroyView()
        Log.d("ttt", "onDestroyView")
        YandexMetrica.reportEvent(TAG + "onDestroyView end")
    }

    override fun onDestroy() {
        //        releasePlayer()
        removePhoneStateCallbacks()
        super.onDestroy()
        Log.d("ttt", "onDestroy")
        YandexMetrica.reportEvent(TAG + "onDestroy end")
    }

    override fun onSurfacesCreated(vlcOut: IVLCVout?) {
        Log.d("ttt", "onSurfacesCreated " + mVideoView)
        //        vlcOut?.attachViews()
        Log.d("tttt", "onSurfacesCreated attached? " + vlcOut?.areViewsAttached())
        Log.d("tttt", "onSurfacesCreated playerstate? " + mMediaPlayer?.playerState)
        YandexMetrica.reportEvent(TAG + "onSurfacesCreated end")
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        Log.d("ttt", "onConfigurationChanged")
        YandexMetrica.reportEvent(TAG + "onConfigurationChanged")
        if (isEndReached) {
            recreateAndPreloadPlayer()
        }

        changeSurfaceLayout()
        super.onConfigurationChanged(newConfig)
        YandexMetrica.reportEvent(TAG + "onConfigurationChanged end")
    }

    override fun onSurfacesDestroyed(vlcOut: IVLCVout?) {
        Log.d("ttt", "onSurfacesDestroyed")
        isOnStartAfterSurfaceDestroyed = true
        YandexMetrica.reportEvent(TAG + "onSurfacesDestroyed")
    }

    override fun onNewLayout(vout: IVLCVout, width: Int, height: Int, visibleWidth: Int, visibleHeight: Int, sarNum: Int, sarDen: Int) {
        if (width * height == 0)
            return
        Log.d("ttt", "onNewLayout")
        // store video size
        mVideoWidth = width
        mVideoHeight = height
        mVideoVisibleWidth = visibleWidth
        mVideoVisibleHeight = visibleHeight
        mSarNum = sarNum
        mSarDen = sarDen
        changeSurfaceLayout()
        YandexMetrica.reportEvent(TAG + "onNewLayout")
    }

    private fun changeSurfaceLayout() {
        YandexMetrica.reportEvent(TAG + "changeSurfaceLayout")
        if (mVideoView == null)
            return

        // get screen size
        var w = activity.getWindow().getDecorView().getWidth().toDouble()
        var h = activity.getWindow().getDecorView().getHeight().toDouble()

        mMediaPlayer?.vlcVout?.setWindowSize(w.toInt(), h.toInt())

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        if (w > h && isPortrait || w < h && !isPortrait) {
            val i = w
            w = h
            h = i
        }

        // sanity check
        if (w * h == 0.toDouble() || mVideoWidth * mVideoHeight == 0) {
            Log.e("ttt", "Invalid surface size")
            YandexMetrica.reportEvent(TAG + "Invalid surface size")
            return
        }


        // compute the aspect ratio
        var ar: Double
        val vw: Double
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth.toDouble()
            ar = mVideoVisibleWidth.toDouble() / mVideoVisibleHeight.toDouble()
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * mSarNum.toDouble() / mSarDen
            ar = vw / mVideoVisibleHeight
        }
        // compute the display aspect ratio
        val dar = w / h

        //        //optimize view (ORIGINAL)
        //        h = mVideoVisibleHeight.toDouble()
        //        w = vw

        ar = 16.0 / 9.0
        if (dar < ar)
            h = w / ar
        else
            w = h * ar

        var lp: ViewGroup.LayoutParams = mVideoView!!.getLayoutParams()
        lp.width = Math.ceil(w.toDouble() * mVideoWidth / mVideoVisibleWidth).toInt()
        lp.height = Math.ceil(h.toDouble() * mVideoHeight / mVideoVisibleHeight).toInt()
        mVideoView!!.setLayoutParams(lp)



        // set frame size (crop if necessary)
        lp = mSurfaceFrame!!.getLayoutParams()
        lp.width = Math.floor(w.toDouble()).toInt()
        lp.height = Math.floor(h.toDouble()).toInt()
        mSurfaceFrame!!.setLayoutParams(lp)

        mVideoView?.invalidate()

    }

    private fun setupController(inflatingView: View?) {
        inflatingView?.let {
            mController = it.findViewById(R.id.player_controller) as TouchDispatchableFrameLayout
            mController?.setParentTouchEvent {
                showController(true)
            }

            if (mController == null) throw RuntimeException()
            mController?.setOnTouchListener { view, motionEvent ->
                true
            }

            mPlayPauseSwitcher = mController?.findViewById(R.id.play_pause_switcher) as ImageSwitcher
            mPauseImageView = mController?.findViewById(R.id.pause_image_view) as ImageView
            mPlayImageView = mController?.findViewById(R.id.play_image_view) as ImageView
            mPlayPauseSwitcher?.setOnClickListener {
                onPlayPause()
            }

            mMediaPlayer?.let {
                if (!it.isReleased) {
                    if (it.isPlaying) {
                        showPause()
                    } else {
                        showPlay()
                    }
                }
            }

            mJumpForwardImageView = mController?.findViewById(R.id.jump_forward_button) as ImageView
            mJumpBackwardImageView = mController?.findViewById(R.id.jump_back_button) as ImageView

            mJumpForwardImageView?.setOnClickListener {
                onJumpForward()
            }

            mJumpBackwardImageView?.setOnClickListener {
                onJumpBackward()
            }

            mVideoRateChooser = mController?.findViewById(R.id.rate_chooser) as ImageView
            mVideoRateChooser?.setImageDrawable(mUserPreferences.videoPlaybackRate.icon)
            mVideoRateChooser?.setOnClickListener {
                showChooseRateMenu(it)
            }

            initSeekBar(mController)
            mSlashTime = mController?.findViewById(R.id.slash_video_time) as TextView
            mCurrentTime = mController?.findViewById(R.id.current_video_time) as TextView
            mMaxTime = mController?.findViewById(R.id.overall_video_time) as TextView
            //            container.addView(mController)
            YandexMetrica.reportEvent(TAG + "setup controller")
        }
    }

    private fun onPlayPause() {
        val index = mPlayPauseSwitcher?.displayedChild
        when (index) {
            INDEX_PLAY_IMAGE -> {
                if (!(mMediaPlayer?.isPlaying ?: true)) {
                    playPlayer() //double checking here =(
                } else if (mMediaPlayer == null) {
                    mPlayPauseSwitcher?.setClickable(false)
                    createPlayer()
                    bindViewWithPlayer()
                    playPlayer()
                    mPlayPauseSwitcher?.showNext()
                }
            }
            INDEX_PAUSE_IMAGE -> {
                pausePlayer()
            }
        }
        YandexMetrica.reportEvent(TAG + "onPlayPause")
    }

    private var mHideRunnable: Runnable? = null

    private fun autoHideController(timeout: Long = TIMEOUT_BEFORE_HIDE) {
        val view = mController
        mHideRunnable?.let {
            view?.removeCallbacks(it)
        }
        if (timeout >= 0) {
            mHideRunnable = Runnable {
                mController?.let {
                    showController(false)
                }
            }
            view?.postDelayed(mHideRunnable, timeout)
        }
        YandexMetrica.reportEvent(TAG + "autoHideController")
    }

    private fun clearAutoHideQueue() {
        val view = mController
        mHideRunnable?.let {
            view?.removeCallbacks(it)
        }
        YandexMetrica.reportEvent(TAG + "clearAutoHideQueue")
    }

    private fun showChooseRateMenu(view: View) {
        YandexMetrica.reportEvent(TAG + "showChooseRateMenu")
        val popupMenu = PopupMenu(MainApplication.getAppContext(), view)
        popupMenu.inflate(R.menu.video_rate_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.x0_5 -> {
                    handleRate(VideoPlaybackRate.x0_5)
                    true
                }

                R.id.x0_75 -> {
                    handleRate(VideoPlaybackRate.x0_75)
                    true
                }

                R.id.x1 -> {
                    handleRate(VideoPlaybackRate.x1_0)
                    true
                }

                R.id.x1_25 -> {
                    handleRate(VideoPlaybackRate.x1_25)
                    true
                }

                R.id.x1_5 -> {
                    handleRate(VideoPlaybackRate.x1_5)
                    true
                }
                R.id.x2 -> {
                    handleRate(VideoPlaybackRate.x2)
                    true
                }

                else -> {
                    false
                }
            }
        }
        popupMenu.show()
    }

    private fun handleRate(rate: VideoPlaybackRate) {
        mVideoRateChooser?.setImageDrawable(rate.icon)
        mMediaPlayer?.rate = rate.rateFloat
        mUserPreferences.videoPlaybackRate = rate
    }

    private fun onJumpForward() {
        YandexMetrica.reportEvent(TAG + "onJumpForward")
        val currentTime = mMediaPlayer?.time
        val maxTime = mMaxTimeInMillis
        if (currentTime != null && maxTime != null && maxTime != 0L) {
            val newTime: Long = Math.min(currentTime + JUMP_TIME_MILLIS, maxTime - JUMP_MAX_DELTA)
            mMediaPlayer?.time = newTime

            val positionByHand = (newTime.toFloat() / maxTime.toFloat()).toFloat()
            mPlayerSeekBar?.let {
                it.progress = (it.max.toFloat() * positionByHand).toInt()
            }
            playPlayer()
        }
    }

    private fun onJumpBackward() {
        YandexMetrica.reportEvent(TAG + "onJumpBackward")
        if (mMediaPlayer == null) {
            createPlayer()
            bindViewWithPlayer()
            val length: Long = mMaxTimeInMillis ?: 0L
            val newTime = Math.max(0L, length - JUMP_TIME_MILLIS)
            playPlayer()
            mMediaPlayer?.time = newTime
        } else {
            val currentTime = mMediaPlayer?.time
            currentTime?.let {
                mMediaPlayer?.time = Math.max(0L, currentTime - JUMP_TIME_MILLIS)
                playPlayer()
            }
        }
    }

    private fun destroyVideoView() {
        mVideoView = null
    }

    private fun destroyController() {
        //todo: implement other
        mPlayPauseSwitcher?.setOnClickListener(null)
        mJumpBackwardImageView?.setOnClickListener(null)
        mJumpForwardImageView?.setOnClickListener(null)
        mVideoRateChooser?.setOnClickListener(null)
        mFragmentContainer?.setOnClickListener(null)

        mFragmentContainer = null
        mVideoRateChooser = null
        mJumpBackwardImageView = null
        mJumpForwardImageView = null
        mPlayerSeekBar = null
        mCurrentTime = null
        mMaxTime = null
        mPlayPauseSwitcher = null
        mPauseImageView = null
        mPlayImageView = null
    }

    private fun initSeekBar(view: View?) {
        mPlayerSeekBar = view?.findViewById(R.id.player_controller_progress) as? AppCompatSeekBar
        mPlayerSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var newPosition = -1f
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (mMediaPlayer?.isReleased ?: true) {
                        createPlayer()
                        bindViewWithPlayer()
                    }
                    newPosition = progress.toFloat() / seekBar.max.toFloat()

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeekBarDragging = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (newPosition >= 0) {
                    mMediaPlayer?.position = newPosition
                    newPosition = -1f
                }
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
                MediaPlayer.Event.Opening -> {
                    Log.d("lala", "MediaPlayer.Event.Opening")
                }
                MediaPlayer.Event.Paused -> {
                    mOwner?.showPlay()
                }
                MediaPlayer.Event.Playing -> {
                    mOwner?.stopLoading()
                    if (player?.isPlaying ?: false) {
                        mOwner?.showPause()
                        player?.length?.let {
                            mOwner?.mSlashTime?.visibility = View.VISIBLE
                            mOwner?.mMaxTimeInMillis = it
                            mOwner?.mMaxTime?.text = TimeUtil.getFormattedVideoTime(it)
                        }
                    }
                }
                MediaPlayer.Event.EndReached -> {
                    mOwner?.isEndReached = true
                    mOwner?.showController(true)
                    mOwner?.showPlay()
                    Log.d("lala", "MediaPlayerEndReached " + player?.time + "/" + player?.length)
                    player?.length?.let {
                        mOwner?.mCurrentTime?.text = TimeUtil.getFormattedVideoTime(it)
                    }
                    mOwner?.mPlayerSeekBar?.let {
                        if (!(mOwner?.isSeekBarDragging ?: false)) {
                            val max = it.max
                            it.progress = max
                        }
                    }
                    mOwner?.releasePlayer()
                }
                MediaPlayer.Event.PositionChanged -> {
                    val currentPos = player?.position

                    currentPos?.let {
                        Log.d("lala", "positionChanged " + it)
                        mOwner?.mPlayerSeekBar?.let {
                            if (!(mOwner?.isSeekBarDragging ?: false)) {
                                val max = it.max
                                it.progress = (max.toFloat() * currentPos).toInt()
                            }
                        }
                    }
                }
                MediaPlayer.Event.TimeChanged -> {
                    player?.time?.let {
                        mOwner?.mCurrentTime?.text =
                                TimeUtil.getFormattedVideoTime(it)
                    }
                }
            }
        }
    }

    private fun showPlay() {
        if (mPlayPauseSwitcher?.displayedChild == INDEX_PAUSE_IMAGE) {
            mPlayPauseSwitcher?.showNext()
        }
    }

    private fun showPause() {
        if (mPlayPauseSwitcher?.displayedChild == INDEX_PLAY_IMAGE) {
            mPlayPauseSwitcher?.showNext()
        }
    }

    private fun pausePlayer() {
        if (mMediaPlayer?.isPlaying ?: false) {
            showController(true, isInfiniteShow = true)
            mFragmentContainer?.keepScreenOn = false
            mMediaPlayer?.pause()
            val isReleased = mAudioFocusHelper.releaseAudioFocus()
            Log.d("ttt", "audio focus isReleased " + isReleased)
        }
    }

    private val preRollListener = PreRollListener(this)

    private class PreRollListener(owner: VideoFragment) : MediaPlayer.EventListener {
        private var mOwner: VideoFragment?

        init {
            mOwner = owner
        }

        override fun onEvent(event: MediaPlayer.Event) {
            val player = mOwner?.mMediaPlayer
            when (event.type) {
                MediaPlayer.Event.Playing -> {
                    YandexMetrica.reportEvent(TAG + "preRollListener playing")
                    //mOwner?.pausePlayer()//it is not need, because we do not want change button
                    player?.pause()
                    player?.setEventListener (mOwner?.mPlayerListener)
                    mOwner?.stopLoading()
                    player?.length?.let {
                        mOwner?.mSlashTime?.visibility = View.VISIBLE
                        mOwner?.mMaxTimeInMillis = it
                        mOwner?.mMaxTime?.text = TimeUtil.getFormattedVideoTime(it)
                        mOwner?.mCurrentTime?.text = TimeUtil.getFormattedVideoTime(mOwner?.mCurrentTimeInMillis ?: 0L)
                        player.time = mOwner?.mCurrentTimeInMillis ?: 0L
                    }
                }
            }
        }
    }

    private fun playPlayer() {
        if (!(mMediaPlayer?.isPlaying ?: true)) {
            mFragmentContainer?.keepScreenOn = true
            val isAudioGained = mAudioFocusHelper.requestAudioFocus()
            mMediaPlayer?.play()
            Log.d("ttt", "isAudioGained " + isAudioGained)
        }
    }

    private fun hideNavigationBar(dim: Boolean = true) {
        YandexMetrica.reportEvent(TAG + "hideNavigationBar")
        if (!AndroidUtil.isHoneycombOrLater())
            return
        var visibility = 0
        var navbar = 0

        if (AndroidUtil.isJellyBeanOrLater()) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        if (dim) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            if (AndroidUtil.isICSOrLater())
                navbar = navbar or View.SYSTEM_UI_FLAG_LOW_PROFILE
            else
                visibility = visibility or View.STATUS_BAR_HIDDEN
            if (!AndroidDevices.hasCombBar()) {
                navbar = navbar or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                if (AndroidUtil.isKitKatOrLater())
                    visibility = visibility or View.SYSTEM_UI_FLAG_IMMERSIVE
                if (AndroidUtil.isJellyBeanOrLater())
                    visibility = visibility or View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            if (AndroidUtil.isICSOrLater())
                visibility = visibility or View.SYSTEM_UI_FLAG_VISIBLE
            else
                visibility = visibility or View.STATUS_BAR_VISIBLE
        }

        if (AndroidDevices.hasNavBar())
            visibility = visibility or navbar
        activity?.window?.decorView?.systemUiVisibility = visibility
    }

    private fun showController(needShow: Boolean, isInfiniteShow: Boolean = false) {
        YandexMetrica.reportEvent(TAG + "showController")
        if (needShow) {
            mController?.visibility = View.VISIBLE
            hideNavigationBar(false)

            if (isEndReached || isInfiniteShow || !(mMediaPlayer?.isPlaying ?: false)) {
                autoHideController(-1)
            } else {
                autoHideController()
            }
            isControllerVisible = true
        } else {
            mController?.visibility = View.GONE
            hideNavigationBar(true)
            isControllerVisible = false
        }
    }


    @Subscribe
    fun onIncomingCall(event: IncomingCallEvent) {
        pausePlayer()
    }

    fun initPhoneStateListener() {
        try {
            YandexMetrica.reportEvent(TAG + "initPhoneStateListener")
            tmgr?.listen(myStatePhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (ex: Exception) {
            YandexMetrica.reportError("initPhoneStateListener", ex)
        }
    }

    fun removePhoneStateCallbacks() {
        try {
            tmgr?.listen(myStatePhoneListener, PhoneStateListener.LISTEN_NONE)
        } catch(ex: Exception) {
            YandexMetrica.reportError("removePhoneStateCallbacks", ex)
        }
    }


    @Subscribe
    fun onAudioFocusLoss(event: AudioFocusLossEvent) {
        pausePlayer()
    }

    fun startLoading() {
        YandexMetrica.reportEvent(TAG + "startLoading")
        isLoading = true
        showController(false)
        mProgressBar?.visibility = View.VISIBLE
    }

    fun stopLoading() {
        YandexMetrica.reportEvent(TAG + "stopLoading")
        mProgressBar?.visibility = View.GONE
        showController(true)
        isLoading = false
    }
}