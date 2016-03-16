package org.stepic.droid.view.fragments

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.AppCompatSeekBar
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.*
import android.widget.*
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.events.IncomingCallEvent
import org.stepic.droid.events.audio.AudioFocusLossEvent
import org.stepic.droid.preferences.VideoPlaybackRate
import org.stepic.droid.util.AndroidDevices
import org.stepic.droid.util.DpPixelsHelper
import org.stepic.droid.util.TimeUtil
import org.stepic.droid.view.custom.TouchDispatchableFrameLayout
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.AndroidUtil
import java.io.File
import java.util.*
import javax.inject.Inject

class VideoFragment : FragmentBase(), LibVLC.HardwareAccelerationError, IVLCVout.Callback {
    companion object {
        private val TIMEOUT_BEFORE_HIDE = 4500L
        private val INDEX_PLAY_IMAGE = 0
        private val INDEX_PAUSE_IMAGE = 1
        private val JUMP_TIME_MILLIS = 10000L
        private val JUMP_MAX_DELTA = 3000L
        private val VIDEO_KEY = "video_key"
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
    private var mPlayerListener: MyPlayerListener? = null
    var mMaxTimeInMillis: Long? = null

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
    var mFullScreenSwitcher: ImageView? = null
    var isControllerVisible = true

    var isEndReachedFirstTime = false

    var isOnStartAfterSurfaceDestroyed = false

    private var mVideoVisibleHeight: Int = 0
    private var mVideoVisibleWidth: Int = 0
    private var mSarNum: Int = 0
    private var mSarDen: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mFilePath = arguments.getString(VIDEO_KEY)
        createPlayer()
        initPhoneStateListener()
        playPlayer()
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentContainer = inflater?.inflate(R.layout.fragment_video, container, false) as? ViewGroup
        mSurfaceFrame = mFragmentContainer?.findViewById(R.id.player_surface_frame) as? FrameLayout
        mVideoView = mFragmentContainer?.findViewById(R.id.texture_video_view) as? SurfaceView
        mFragmentContainer?.setOnTouchListener { view, motionEvent ->
            Log.d("ttt", "mFragmentContainer?.setOnTouchListener  " + view.javaClass.canonicalName)
            showController(!isControllerVisible)
            false
        }

        setupController(mFragmentContainer)
        bindViewWithPlayer()
        playPlayer()
        isOnStartAfterSurfaceDestroyed = false
        Log.d("ttt", "onCreateView")
        return mFragmentContainer
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        hideNavigationBar(false)
        //        activity?.requestedOrientation = getScreenOrientation()

    }


    private fun bindViewWithPlayer() {
        val vout = mMediaPlayer?.getVLCVout()
        vout?.setVideoView(mVideoView)
        vout?.addCallback(this)
        vout?.attachViews()

        mPlayerListener = MyPlayerListener(this)
        mMediaPlayer?.setEventListener(mPlayerListener)

        mPlayPauseSwitcher?.setClickable(true)
    }

    private fun createPlayer() {
        try {
            val options = ArrayList<String>()

            options.add("--audio-time-stretch") // time stretching
            options.add("--no-drop-late-frames") //help when user accelerates video
            options.add("--no-skip-frames")
            options.add("-vvv") // verbosity
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
            val m = Media(libvlc, uri)
            mMediaPlayer?.setMedia(m)

            mMediaPlayer?.setRate(mUserPreferences.videoPlaybackRate.rateFloat)
            isEndReachedFirstTime = false
        } catch (e: Exception) {
            Toast.makeText(activity, "Error creating player!", Toast.LENGTH_LONG).show()
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
        //throw UnsupportedOperationException()
        //fixme: recreate player
        YandexMetrica.reportEvent("vlc error hardware")
    }

    override fun onStart() {
        super.onStart()
        Log.d("ttt", "onStart")
        bus.register(this)
    }


    override fun onResume() {
        super.onResume()
        if (isOnStartAfterSurfaceDestroyed) {
            bindViewWithPlayer()
            isOnStartAfterSurfaceDestroyed = false
        }
        Log.d("ttt", "onResume")
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
        Log.d("ttt", "onPause")
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
        Log.d("ttt", "onStop")
    }

    override fun onDestroyView() {
        destroyVideoView()
        destroyController()
        super.onDestroyView()
        Log.d("ttt", "onDestroyView")
    }

    override fun onDestroy() {
        releasePlayer()
        removePhoneStateCallbacks()
        super.onDestroy()
        Log.d("ttt", "onDestroy")
    }

    override fun onSurfacesCreated(vlcOut: IVLCVout?) {
        Log.d("ttt", "onSurfacesCreated " + mVideoView)
//        vlcOut?.attachViews()
        Log.d("tttt", "onSurfacesCreated attached? " + vlcOut?.areViewsAttached())
        Log.d("tttt", "onSurfacesCreated playerstate? " + mMediaPlayer?.playerState)
        val i  =100
        val j =102
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        Log.d("ttt", "onConfigurationChanged")

        val widthPx = DpPixelsHelper.convertDpToPixel(newConfig?.screenWidthDp?.toFloat() ?: 0f).toInt()
        val heightPx = DpPixelsHelper.convertDpToPixel(newConfig?.screenHeightDp?.toFloat() ?: 0f).toInt()

        Log.d("ttt", "width = " + newConfig?.screenWidthDp + " height = " + newConfig?.screenHeightDp)
        Log.d("ttt", "width = " + widthPx + " height = " + heightPx)

        changeSurfaceLayout()
    }

    override fun onSurfacesDestroyed(vlcOut: IVLCVout?) {
        Log.d("ttt", "onSurfacesDestroyed")
        isOnStartAfterSurfaceDestroyed = true

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
    }

    private fun changeSurfaceLayout() {
        if (mVideoView == null)
            return

        // get screen size
        var w = activity.getWindow().getDecorView().getWidth().toDouble()
        var h = activity.getWindow().getDecorView().getHeight().toDouble()

        Log.d("ttt", "decorview w " + w)
        Log.d("ttt", "decorview h " + h)
        Log.d("ttt", "videowidth " + mVideoWidth)
        Log.d("ttt", "videoheight " + mVideoHeight)
        Log.d("ttt", "video visible width " + mVideoVisibleWidth)
        Log.d("ttt", "video visible height " + mVideoVisibleHeight)

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







        //        // force surface buffer size
        //        mMediaPlayer?.vlcVout?.setWindowSize(mVideoWidth, mVideoHeight)
        //        //        mVideoViewHolder?.setFixedSize(mVideoWidth, mVideoHeight)
        //
        //        // set display size
        //        val lp = mVideoView?.getLayoutParams()
        //        lp?.width = w
        //        lp?.height = h
        //        mVideoView?.setLayoutParams(lp)
        //        mVideoView?.invalidate()
    }

    private fun setupController(inflatingView: View?) {
        inflatingView?.let {
            mController = it.findViewById(R.id.player_controller) as TouchDispatchableFrameLayout
            mController?.setParentTouchEvent {
                autoHideController()
            }

            if (mController == null) throw RuntimeException()
            mController?.setOnTouchListener { view, motionEvent ->
                true
            }
            autoHideController()

            mPlayPauseSwitcher = mController?.findViewById(R.id.play_pause_switcher) as? ImageSwitcher
            mPauseImageView = mController?.findViewById(R.id.pause_image_view) as? ImageView
            mPlayImageView = mController?.findViewById(R.id.play_image_view) as? ImageView
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

            mJumpForwardImageView = mController?.findViewById(R.id.jump_forward_button) as? ImageView
            mJumpBackwardImageView = mController?.findViewById(R.id.jump_back_button) as? ImageView

            mJumpForwardImageView?.setOnClickListener {
                onJumpForward()
            }

            mJumpBackwardImageView?.setOnClickListener {
                onJumpBackward()
            }

            mVideoRateChooser = mController?.findViewById(R.id.rate_chooser) as? ImageView
            mVideoRateChooser?.setImageDrawable(mUserPreferences.videoPlaybackRate.icon)
            mVideoRateChooser?.setOnClickListener {
                showChooseRateMenu(it)
            }

            initFullScreenButton(mController)

            initSeekBar(mController)
            mCurrentTime = mController?.findViewById(R.id.current_video_time) as? TextView
            mMaxTime = mController?.findViewById(R.id.overall_video_time) as? TextView
            //            container.addView(mController)
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
    }

    private fun initFullScreenButton(controller: View?) {
        mFullScreenSwitcher = mController?.findViewById(R.id.full_screen_switcher) as? ImageView

        val display = (activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager).getDefaultDisplay()
        val rotation = display.rotation
        when (rotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                //portrait
                mFullScreenSwitcher?.setImageResource(R.drawable.ic_fullscreen_white_24px)
            }

            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                //landscape
                mFullScreenSwitcher?.setImageResource(R.drawable.ic_fullscreen_exit_white_24px)
            }
        }
        mFullScreenSwitcher?.setOnClickListener { onClickFullScreen() }
    }

    private fun onClickFullScreen() {
        val display = (activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager).getDefaultDisplay()
        val rotation = display.rotation
        when (rotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                //portrait
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                //landscape
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    private fun showChooseRateMenu(view: View) {
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
        mFullScreenSwitcher?.setOnClickListener(null)
        mFragmentContainer?.setOnClickListener(null)

        mFragmentContainer = null
        mFullScreenSwitcher = null
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
                MediaPlayer.Event.Paused -> {
                    mOwner?.showPlay()
                }
                MediaPlayer.Event.Playing -> {
                    if (player?.isPlaying ?: false) {
                        mOwner?.showPause()
                        player?.length?.let {
                            mOwner?.mMaxTimeInMillis = it
                            mOwner?.mMaxTime?.text = TimeUtil.getFormattedVideoTime(it)
                        }
                    }
                }
                MediaPlayer.Event.EndReached -> {
                    mOwner?.isEndReachedFirstTime = true
                    mOwner?.showController(true)
                    mOwner?.showPlay()
                    Log.d("lala", "MediaPlayerEndReached " + player?.time + "/" + player?.length)
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
                            if (!(mOwner?.isSeekBarDragging ?: false)) {
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
            mFragmentContainer?.keepScreenOn = false
            mMediaPlayer?.pause()
            val isReleased = mAudioFocusHelper.releaseAudioFocus()
            Log.d("ttt", "audio focus isReleased " + isReleased)
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
        Log.d("ttt", "hideNavigationBar " + dim)
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

        //        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        //        activity?.window?.decorView?.systemUiVisibility = uiOptions
    }

    private fun showController(needShow: Boolean) {
        Log.d("tttt", "showController(" + needShow + ")" + mController?.visibility)
        if (needShow) {
            mController?.visibility = View.VISIBLE
            hideNavigationBar(false)
            //            if (activity?.window?.decorView?.systemUiVisibility != 0) {
            //                activity?.window?.decorView?.systemUiVisibility = 0
            //            }
            if (!isEndReachedFirstTime) {
                autoHideController()
            } else {
                autoHideController(-1)
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

    class MyStatePhoneListener : PhoneStateListener() {

        init {
            MainApplication.component().inject(this)
        }

        @Inject
        lateinit var mBus: Bus

        @Inject
        lateinit var mHandler: IMainHandler

        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            if (state == 1) {
                mHandler.post { mBus.post(IncomingCallEvent()) }
            }
        }
    }

    @Subscribe
    fun onAudioFocusLoss(event: AudioFocusLossEvent) {
        pausePlayer()
    }
}