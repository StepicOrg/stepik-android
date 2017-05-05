package org.stepic.droid.ui.fragments

import org.stepic.droid.base.FragmentBase

class VideoFragment : FragmentBase()
//        , IVLCVout.Callback,
//        VideoWithTimestampView
{
//
//    companion object {
//        private val TIMEOUT_BEFORE_HIDE = 4500L
//        private val INDEX_PLAY_IMAGE = 0
//        private val INDEX_PAUSE_IMAGE = 1
//        private val JUMP_TIME_MILLIS = 10000L
//        private val JUMP_MAX_DELTA = 3000L
//        private val VIDEO_PATH_KEY = "video_path_key"
//        private val VIDEO_ID_KEY = "video_id_key"
//        private val DELTA_TIME = 0L
//        fun newInstance(videoUri: String, videoId: Long): VideoFragment {
//            val args = Bundle()
//            args.putString(VIDEO_PATH_KEY, videoUri)
//            args.putLong(VIDEO_ID_KEY, videoId)
//            val fragment = VideoFragment()
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//    val myStatePhoneListener = MyPhoneStateListener()
//    val tmgr = App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
//    var surfaceFrame: FrameLayout? = null
//    var fragmentContainer: ViewGroup? = null
//    var videoView: SurfaceView? = null;
//    var filePath: String? = null;
//    private var videoId: Long? = null
//    var libvlc: LibVLC? = null
//    var mediaPlayer: MediaPlayer? = null
//    var videoWidth: Int = 0
//    var videoHeight: Int = 0
//    private val playerListener: MyPlayerListener = MyPlayerListener(this)
//    var maxTimeInMillis: Long? = null
//    var currentTimeInMillis: Long = 0L
//    var progressBar: ProgressBar? = null
//
//    var isSeekBarDragging: Boolean = false
//
//    //Controller:
//    var controller: TouchDispatchableFrameLayout? = null
//    var playerSeekBar: AppCompatSeekBar? = null
//    var currentTime: TextView? = null
//    var maxTime: TextView? = null
//    var playPauseSwitcher: ImageSwitcher? = null
//    var playImageView: ImageView? = null
//    var pauseImageView: ImageView? = null
//    var jumpForwardImageView: ImageView? = null
//    var jumpBackwardImageView: ImageView? = null
//    var videoRateChooser: ImageView? = null
//    private var slashTime: TextView? = null
//    var isControllerVisible = true
//
//    var isEndReached = false
//
//    var isOnStartAfterSurfaceDestroyed = false
//
//    private var mVideoVisibleHeight: Int = 0
//    private var mVideoVisibleWidth: Int = 0
//    private var mSarNum: Int = 0
//    private var mSarDen: Int = 0
//    private var isOnResumeDirectlyAfterOnCreate = true
//    private var hideRunnable: Runnable? = null
//    private val preRollListener = PreRollListener(this)
//    private var needPlay = false
//
//    private var isLoading: Boolean = false
//
//    private val receiver: BroadcastReceiver = MyBroadcastReceiver(this)
//
//    @Inject
//    lateinit var videoTimestampPresenter: VideoWithTimestampPresenter
//
//    override fun injectComponent() {
//        App
//                .component()
//                .videoComponentBuilder()
//                .build()
//                .inject(this)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        retainInstance = true
//        filePath = arguments.getString(VIDEO_PATH_KEY)
//        videoId = arguments.getLong(VIDEO_ID_KEY)
//        if (videoId != null && videoId!! <= 0L) { // if equal zero -> it is default, it is not our video
//            videoId = null
//        }
//        initPhoneStateListener()
//        isOnResumeDirectlyAfterOnCreate = true
//    }
//
//
//    private class MyBroadcastReceiver(owner: VideoFragment) : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            when (intent?.action) {
//                AudioManager.ACTION_AUDIO_BECOMING_NOISY ->
//                    mOwner?.pausePlayer()
//            }
//
//        }
//
//        private var mOwner: VideoFragment?
//
//        init {
//            mOwner = owner
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        fragmentContainer = inflater?.inflate(R.layout.fragment_video, container, false) as ViewGroup
//
//        progressBar = fragmentContainer?.findViewById(R.id.load_progressbar) as ProgressBar
//        progressBar?.visibility = View.VISIBLE
//
////        surfaceFrame = fragmentContainer?.findViewById(R.id.player_surface_frame) as FrameLayout
//        videoView = fragmentContainer?.findViewById(R.id.videoSurfaceView) as SurfaceView
//        fragmentContainer?.setOnTouchListener { _, _ ->
//            if (true || !isLoading) {
//                showController(!isControllerVisible)
//            }
//            false
//        }
//        setupController(fragmentContainer)
//        isOnStartAfterSurfaceDestroyed = false
//
//        val filter = IntentFilter()
//        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
//        activity.registerReceiver(receiver, filter)
//        startLoading()
//        return fragmentContainer
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        hideNavigationBar(false)
//        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
//    }
//
//    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        videoTimestampPresenter.attachView(this)
//    }
//
//    private fun bindViewWithPlayer() {
//        val vout = mediaPlayer?.vlcVout
//        vout?.setVideoView(videoView)
//        vout?.addCallback(this)
//        vout?.attachViews()
//
//        mediaPlayer?.setEventListener(playerListener)
//
//        playPauseSwitcher?.isClickable = true
//
//    }
//
//    private fun createPlayer() {
//        try {
//            val options = ArrayList<String>()
//
//            options.add("--audio-time-stretch") // time stretching
//            options.add("--no-drop-late-frames") //help when user accelerates video
//            options.add("--no-skip-frames")
//
//            libvlc = LibVLC(options)
//
//            // Create media player
//            mediaPlayer = MediaPlayer(libvlc)
//
//            val file = File(filePath)
//            var uri: Uri?
//            if (file.exists()) {
//                uri = Uri.fromFile(file)
//            } else {
//                uri = Uri.parse(filePath)
//                if (uri?.scheme == null) {
//                    uri = Uri.parse(AppConstants.FILE_SCHEME_PREFIX + filePath)
//                }
//            }
//
//            val media = Media(libvlc, uri)
//            mediaPlayer?.media = media
//            media.release()
//
//            mediaPlayer?.rate = userPreferences.videoPlaybackRate.rateFloat
//            isEndReached = false
//        } catch (e: Exception) {
//            analytic.reportError(Analytic.Error.ERROR_CREATING_PLAYER, e)
//        }
//
//    }
//
//    private fun releasePlayer() {
//        mediaPlayer?.stop()
//        val vout = mediaPlayer?.vlcVout
//        vout?.removeCallback(this)
//        vout?.detachViews()
//        mediaPlayer?.setEventListener(null)
//        libvlc?.release()
//        libvlc = null
//        mediaPlayer?.release()
//        mediaPlayer = null
//        videoWidth = 0
//        videoHeight = 0
//    }
//
//    override fun onHardwareAccelerationError(vlcVout: IVLCVout?) {
//        analytic.reportEvent(Analytic.Video.VLC_HARDWARE_ERROR)
//        Timber.d("onHardwareAccelerationError")
//        releasePlayer()
//        recreateAndPreloadPlayer(isNeedPlayAfterRecreating = true)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Timber.d("onResume")
//        bus.register(this)
//        videoTimestampPresenter.showVideoWithPredefinedTimestamp(videoId)
//    }
//
//    fun recreateAndPreloadPlayer(isNeedPlayAfterRecreating: Boolean = true) {
//        needPlay = isNeedPlayAfterRecreating
//        val km = App.getAppContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
//        createPlayer()
//        bindViewWithPlayer()
//
//        if (!km.inKeyguardRestrictedInputMode()) {
//            if (!needPlay && !isEndReached) {
//                startLoading()
//            }
//            if (isOnResumeDirectlyAfterOnCreate) {
//                isOnResumeDirectlyAfterOnCreate = false
//                mediaPlayer?.setEventListener(playerListener)
//                playPlayer()
//            } else {
//                mediaPlayer?.setEventListener(preRollListener)
//                mediaPlayer?.play()
//            }
//            playerSeekBar?.let {
//                if (!isSeekBarDragging) {
//                    val max = it.max
//                    var positionByHand = 0f
//                    if (maxTimeInMillis != null) {
//                        val maxTime = maxTimeInMillis ?: 1L
//                        positionByHand = (currentTimeInMillis.toFloat() / maxTime.toFloat()).toFloat()
//                    }
//                    if (positionByHand > max) {
//                        positionByHand = 0f
//                    }
//
//                    it.progress = (max.toFloat() * positionByHand).toInt()
//                }
//            }
//        } else {
//            showController(false)
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Timber.d("onPause")
//        stopPlayingBeforeRecreating()
//
//        clearAutoHideQueue()
//        audioFocusHelper.releaseAudioFocus()
//        bus.unregister(this)
//    }
//
//    fun stopPlayingBeforeRecreating() {
//        showPlay() // because callback not working here
//        val player = mediaPlayer
//        if (player == null || player.isReleased) {
//            currentTimeInMillis = 0L
//        } else if (player.time >= 0) {
//            currentTimeInMillis = (mediaPlayer?.time ?: 0L) - DELTA_TIME
//        }
//        if (currentTimeInMillis < 0L) currentTimeInMillis = 0L
//        videoTimestampPresenter.saveMillis(currentTimeInMillis, videoId)
//        pausePlayer()
//        mediaPlayer?.setEventListener(null)
//        releasePlayer()
//    }
//
//    override fun onDestroyView() {
//        videoTimestampPresenter.detachView(this)
//        destroyVideoView()
//        destroyController()
//        activity?.unregisterReceiver(receiver)
//        super.onDestroyView()
//    }
//
//    override fun onDestroy() {
//        removePhoneStateCallbacks()
//        super.onDestroy()
//    }
//
//    override fun onSurfacesCreated(vlcOut: IVLCVout?) {
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration?) {
//        if (isEndReached) {
//            recreateAndPreloadPlayer(isNeedPlayAfterRecreating = false)
//        }
//
//        changeSurfaceLayout()
//        super.onConfigurationChanged(newConfig)
//    }
//
//    override fun onSurfacesDestroyed(vlcOut: IVLCVout?) {
//        isOnStartAfterSurfaceDestroyed = true
//    }
//
//    override fun onNewLayout(vout: IVLCVout, width: Int, height: Int, visibleWidth: Int, visibleHeight: Int, sarNum: Int, sarDen: Int) {
//        if (width * height == 0)
//            return
//        // store video size
//        videoWidth = width
//        videoHeight = height
//        mVideoVisibleWidth = visibleWidth
//        mVideoVisibleHeight = visibleHeight
//        mSarNum = sarNum
//        mSarDen = sarDen
//        changeSurfaceLayout()
//    }
//
//    private fun changeSurfaceLayout() {
//        if (videoView == null)
//            return
//
//        // get screen size
//        var w = activity.window.decorView.width.toDouble()
//        var h = activity.window.decorView.height.toDouble()
//
//        mediaPlayer?.vlcVout?.setWindowSize(w.toInt(), h.toInt())
//
//        // getWindow().getDecorView() doesn't always take orientation into
//        // account, we have to correct the values
//        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
//        if (w > h && isPortrait || w < h && !isPortrait) {
//            val i = w
//            w = h
//            h = i
//        }
//
//        // sanity check
//        if (w * h == 0.toDouble() || videoWidth * videoHeight == 0) {
//            analytic.reportEvent(Analytic.Video.INVALID_SURFACE_SIZE)
//            return
//        }
//
//
//        // compute the aspect ratio
//        var ar: Double
//        //        val vw: Double
//        if (mSarDen == mSarNum) {
//            /* No indication about the density, assuming 1:1 */
//            //            vw = mVideoVisibleWidth.toDouble()
//            //            ar = mVideoVisibleWidth.toDouble() / mVideoVisibleHeight.toDouble()
//        } else {
//            /* Use the specified aspect ratio */
//            //            vw = mVideoVisibleWidth * mSarNum.toDouble() / mSarDen
//            //            ar = vw / mVideoVisibleHeight
//        }
//        // compute the display aspect ratio
//        val dar = w / h
//
//        //        //optimize view (ORIGINAL)
//        //        h = mVideoVisibleHeight.toDouble()
//        //        w = vw
//
//        ar = 16.0 / 9.0
//        if (dar < ar)
//            h = w / ar
//        else
//            w = h * ar
//
//        var lp: ViewGroup.LayoutParams = videoView!!.layoutParams
//        lp.width = Math.ceil(w.toDouble() * videoWidth / mVideoVisibleWidth).toInt()
//        lp.height = Math.ceil(h.toDouble() * videoHeight / mVideoVisibleHeight).toInt()
//        videoView!!.layoutParams = lp
//
//
//        // set frame size (crop if necessary)
//        lp = surfaceFrame!!.layoutParams
//        lp.width = Math.floor(w.toDouble()).toInt()
//        lp.height = Math.floor(h.toDouble()).toInt()
//        surfaceFrame!!.layoutParams = lp
//
//        videoView?.invalidate()
//
//    }
//
//    private fun setupController(inflatingView: View?) {
//        inflatingView?.let {
//            controller = it.findViewById(R.id.player_controller) as TouchDispatchableFrameLayout
//            controller?.setParentTouchEvent {
//                showController(true)
//            }
//
//            if (controller == null) throw RuntimeException()
//            controller?.setOnTouchListener { _, _ ->
//                true
//            }
//
//            playPauseSwitcher = controller?.findViewById(R.id.play_pause_switcher) as ImageSwitcher
//            pauseImageView = controller?.findViewById(R.id.pause_image_view) as ImageView
//            playImageView = controller?.findViewById(R.id.play_image_view) as ImageView
//            playPauseSwitcher?.setOnClickListener {
//                onPlayPause()
//            }
//
//            mediaPlayer?.let {
//                if (!it.isReleased) {
//                    if (it.isPlaying) {
//                        showPause()
//                    } else {
//                        showPlay()
//                    }
//                }
//            }
//
//            jumpForwardImageView = controller?.findViewById(R.id.jump_forward_button) as ImageView
//            jumpBackwardImageView = controller?.findViewById(R.id.jump_back_button) as ImageView
//
//            jumpForwardImageView?.setOnClickListener {
//                onJumpForward()
//            }
//
//            jumpBackwardImageView?.setOnClickListener {
//                onJumpBackward()
//            }
//
//            videoRateChooser = controller?.findViewById(R.id.rate_chooser) as ImageView
//            videoRateChooser?.setImageDrawable(userPreferences.videoPlaybackRate.icon)
//            videoRateChooser?.setOnClickListener {
//                showChooseRateMenu(it)
//            }
//
//            initSeekBar(controller)
//            slashTime = controller?.findViewById(R.id.slash_video_time) as TextView
//            currentTime = controller?.findViewById(R.id.current_video_time) as TextView
//            maxTime = controller?.findViewById(R.id.overall_video_time) as TextView
//        }
//    }
//
//    private fun onPlayPause() {
//        val index = playPauseSwitcher?.displayedChild
//        when (index) {
//            INDEX_PLAY_IMAGE -> {
//                if (!(mediaPlayer?.isPlaying ?: true)) {
//                    playPlayer() //double checking here =(
//                } else if (mediaPlayer == null) {
//                    playPauseSwitcher?.isClickable = false
//                    createPlayer()
//                    bindViewWithPlayer()
//                    playPlayer()
//                    playPauseSwitcher?.showNext()
//                }
//            }
//            INDEX_PAUSE_IMAGE -> {
//                pausePlayer()
//            }
//        }
//    }
//
//    private fun autoHideController(timeout: Long = TIMEOUT_BEFORE_HIDE) {
//        val view = controller
//        hideRunnable?.let {
//            view?.removeCallbacks(it)
//        }
//        if (timeout >= 0) {
//            hideRunnable = Runnable {
//                controller?.let {
//                    showController(false)
//                }
//            }
//            view?.postDelayed(hideRunnable, timeout)
//        }
//    }
//
//    private fun clearAutoHideQueue() {
//        val view = controller
//        hideRunnable?.let {
//            view?.removeCallbacks(it)
//        }
//    }
//
//    private fun showChooseRateMenu(view: View) {
//        analytic.reportEvent(Analytic.Video.SHOW_CHOOSE_RATE)
//        val popupMenu = PopupMenu(App.getAppContext(), view)
//        popupMenu.inflate(R.menu.video_rate_menu)
//        popupMenu.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.x0_5 -> {
//                    handleRate(VideoPlaybackRate.x0_5)
//                    true
//                }
//
//                R.id.x0_75 -> {
//                    handleRate(VideoPlaybackRate.x0_75)
//                    true
//                }
//
//                R.id.x1 -> {
//                    handleRate(VideoPlaybackRate.x1_0)
//                    true
//                }
//
//                R.id.x1_25 -> {
//                    handleRate(VideoPlaybackRate.x1_25)
//                    true
//                }
//
//                R.id.x1_5 -> {
//                    handleRate(VideoPlaybackRate.x1_5)
//                    true
//                }
//                R.id.x1_75 -> {
//                    handleRate(VideoPlaybackRate.x1_75)
//                    true
//                }
//                R.id.x2 -> {
//                    handleRate(VideoPlaybackRate.x2)
//                    true
//                }
//
//                else -> {
//                    false
//                }
//            }
//        }
//        popupMenu.show()
//    }
//
//    private fun handleRate(rate: VideoPlaybackRate) {
//        videoRateChooser?.setImageDrawable(rate.icon)
//        mediaPlayer?.rate = rate.rateFloat
//        userPreferences.videoPlaybackRate = rate
//    }
//
//    private var isInitiatedByTimestamp: Boolean = false
//
//    override fun onNeedShowVideoWithTimestamp(timestamp: Long) {
//        if (!isInitiatedByTimestamp) {
//            currentTimeInMillis = Math.max(timestamp - JUMP_MAX_DELTA, 0L)
//        }
//        isInitiatedByTimestamp = true
//        releasePlayer()
//        recreateAndPreloadPlayer(isNeedPlayAfterRecreating = false)
//    }
//
//    private fun onJumpForward() {
//        analytic.reportEvent(Analytic.Video.JUMP_FORWARD)
//        var currentTime = mediaPlayer?.time
//        if (currentTime == 0L && currentTimeInMillis > 0L) {
//            currentTime = currentTimeInMillis
//        }
//        val maxTime = maxTimeInMillis
//        if (currentTime != null && maxTime != null && maxTime != 0L) {
//            val newTime: Long = Math.min(currentTime + JUMP_TIME_MILLIS, maxTime - JUMP_MAX_DELTA)
//
//            val positionByHand = (newTime.toFloat() / maxTime.toFloat()).toFloat()
//            playerSeekBar?.let {
//                it.progress = (it.max.toFloat() * positionByHand).toInt()
//            }
//            currentTimeInMillis = newTime
//
//            pausePlayer(releaseAudioFocusAndScreen = false)
//            mediaPlayer?.setEventListener(null)
//            releasePlayer()
//            recreateAndPreloadPlayer()
//        }
//    }
//
//    private fun onJumpBackward() {
//        analytic.reportEvent(Analytic.Video.JUMP_BACKWARD)
//        if (mediaPlayer == null) {
//            val length: Long = maxTimeInMillis ?: 0L
//            currentTimeInMillis = Math.max(0L, length - JUMP_TIME_MILLIS)
//
//            pausePlayer(releaseAudioFocusAndScreen = false)
//            mediaPlayer?.setEventListener(null)
//            releasePlayer()
//            recreateAndPreloadPlayer()
//
//        } else {
//            var currentTime = mediaPlayer?.time ?: 0L
//            if (currentTime == 0L && currentTimeInMillis > 0L) {
//                currentTime = currentTimeInMillis
//            }
//            currentTimeInMillis = Math.max(0L, currentTime.toLong() - JUMP_TIME_MILLIS)
//
//            pausePlayer(releaseAudioFocusAndScreen = false)
//            mediaPlayer?.setEventListener(null)
//            releasePlayer()
//            recreateAndPreloadPlayer()
//        }
//    }
//
//    private fun destroyVideoView() {
//        videoView = null
//    }
//
//    private fun destroyController() {
//        playPauseSwitcher?.setOnClickListener(null)
//        jumpBackwardImageView?.setOnClickListener(null)
//        jumpForwardImageView?.setOnClickListener(null)
//        videoRateChooser?.setOnClickListener(null)
//        fragmentContainer?.setOnClickListener(null)
//
//        fragmentContainer = null
//        videoRateChooser = null
//        jumpBackwardImageView = null
//        jumpForwardImageView = null
//        playerSeekBar = null
//        currentTime = null
//        maxTime = null
//        playPauseSwitcher = null
//        pauseImageView = null
//        playImageView = null
//    }
//
//    private fun initSeekBar(view: View?) {
//        playerSeekBar = view?.findViewById(R.id.player_controller_progress) as? AppCompatSeekBar
//        playerSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            var newPosition = -1f
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                if (fromUser) {
//                    if (mediaPlayer?.isReleased ?: true) {
//                        createPlayer()
//                        bindViewWithPlayer()
//                    }
//                    newPosition = progress.toFloat() / seekBar.max.toFloat()
//
//                }
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//                isSeekBarDragging = true
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                if (newPosition >= 0) {
//                    val seekToTime: Float = maxTimeInMillis?.toFloat() ?: 0f
//                    currentTimeInMillis = (newPosition * seekToTime).toLong()
//
//                    pausePlayer(releaseAudioFocusAndScreen = false)
//                    mediaPlayer?.setEventListener(null)
//                    releasePlayer()
//                    recreateAndPreloadPlayer()
//                    newPosition = -1f
//                }
//                isSeekBarDragging = false
//            }
//        })
//    }
//
//    private class MyPlayerListener(owner: VideoFragment) : MediaPlayer.EventListener {
//        private var owner: VideoFragment?
//
//        init {
//            this.owner = owner
//        }
//
//        private var alreadyTimestamped = false
//
//        override fun onEvent(event: MediaPlayer.Event) {
//            val player = owner?.mediaPlayer
//            when (event.type) {
//                MediaPlayer.Event.Paused -> {
//                    Timber.d("player paused")
//                    owner?.showPlay()
//                }
//                MediaPlayer.Event.Playing -> {
//                    Timber.d("player playing")
//                    owner?.stopLoading()
//
//                    if (!alreadyTimestamped) {
//                        var currentTime = 0L
//                        if (owner?.currentTimeInMillis != null) {
//                            currentTime = owner!!.currentTimeInMillis
//                        }
//                        player?.let {
//                            if (it.length - JUMP_MAX_DELTA < currentTime) {
//                                it.time = 0L
//                            } else {
//                                it.time = currentTime
//                            }
//                        }
//                        alreadyTimestamped = true
//                    }
//                    if (player?.isPlaying ?: false) {
//                        owner?.showPause()
//                        player?.length?.let {
//                            owner?.slashTime?.visibility = View.VISIBLE
//                            owner?.maxTimeInMillis = it
//                            owner?.maxTime?.text = TimeUtil.getFormattedVideoTime(it)
//                        }
//                    }
//                }
//                MediaPlayer.Event.EndReached -> {
//                    Timber.d("player endReached")
//                    owner?.activity?.finish()
//                    owner?.isEndReached = true
//                    owner?.showController(true)
//                    owner?.showPlay()
//                    player?.length?.let {
//                        owner?.currentTime?.text = TimeUtil.getFormattedVideoTime(it)
//                    }
//                    owner?.playerSeekBar?.let {
//                        if (!(owner?.isSeekBarDragging ?: false)) {
//                            val max = it.max
//                            it.progress = max
//                        }
//                    }
//                    owner?.releasePlayer()
//                    owner?.activity?.finish()
//                }
//                MediaPlayer.Event.PositionChanged -> {
//                    val currentPos = player?.position
//                    currentPos?.let {
//                        owner?.playerSeekBar?.let {
//                            if (!(owner?.isSeekBarDragging ?: false)) {
//                                val max = it.max
//                                it.progress = (max.toFloat() * currentPos).toInt()
//                            }
//                        }
//                    }
//                }
//                MediaPlayer.Event.TimeChanged -> {
//                    player?.time?.let {
//                        owner?.currentTime?.text =
//                                TimeUtil.getFormattedVideoTime(it)
//                    }
//                }
//                MediaPlayer.Event.Stopped -> {
//                    Timber.d("player stopped")
//                    owner?.activity?.let {
//                        Toast.makeText(it, R.string.sync_problem, Toast.LENGTH_SHORT).show()
//                        it.finish()
//                    }
//                }
//
//            }
//        }
//    }
//
//    private fun showPlay() {
//        if (playPauseSwitcher?.displayedChild == INDEX_PAUSE_IMAGE) {
//            playPauseSwitcher?.showNext()
//        }
//    }
//
//    private fun showPause() {
//        if (playPauseSwitcher?.displayedChild == INDEX_PLAY_IMAGE) {
//            playPauseSwitcher?.showNext()
//        }
//    }
//
//    private fun pausePlayer(releaseAudioFocusAndScreen: Boolean = true) {
//        if (mediaPlayer?.isPlaying ?: false) {
//            showController(true, isInfiniteShow = true)
//
//            mediaPlayer?.pause()
//            if (releaseAudioFocusAndScreen) {
//                fragmentContainer?.keepScreenOn = false
//                audioFocusHelper.releaseAudioFocus()
//            }
//        }
//    }
//
//    private class PreRollListener(owner: VideoFragment) : MediaPlayer.EventListener {
//        private var owner: VideoFragment?
//
//        init {
//            this.owner = owner
//        }
//
//        override fun onEvent(event: MediaPlayer.Event) {
//            val player = owner?.mediaPlayer
//            when (event.type) {
//                MediaPlayer.Event.Playing -> {
//                    Timber.d("pre roll Playing")
//                    //mOwner?.pausePlayer()//it is not need, because we do not want change button
//                    player?.pause()
//                    player?.setEventListener(owner?.playerListener)
//                    owner?.stopLoading()
//                    player?.length?.let {
//                        owner?.slashTime?.visibility = View.VISIBLE
//                        owner?.maxTimeInMillis = it
//                        owner?.maxTime?.text = TimeUtil.getFormattedVideoTime(it)
//                        owner?.currentTime?.text = TimeUtil.getFormattedVideoTime(owner?.currentTimeInMillis ?: 0L)
//                        player.time = owner?.currentTimeInMillis ?: 0L
//                    }
//                    if (owner?.needPlay ?: false) {
//                        owner?.fragmentContainer?.keepScreenOn = true
//                        owner?.audioFocusHelper?.requestAudioFocus()
//                        player?.play()
//                    }
//                }
//
//                MediaPlayer.Event.Stopped -> {
//                    Timber.d("video stopped preroll")
//                }
//            }
//        }
//    }
//
//    private fun playPlayer() {
//        if (!(mediaPlayer?.isPlaying ?: true)) {
//            fragmentContainer?.keepScreenOn = true
//            audioFocusHelper.requestAudioFocus()
//            mediaPlayer?.play()
//        }
//    }
//
//    private fun hideNavigationBar(dim: Boolean = true) {
//        if (!AndroidUtil.isHoneycombOrLater())
//            return
//        var visibility = 0
//        var navbar = 0
//
//        if (AndroidUtil.isJellyBeanOrLater()) {
//            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        }
//        if (dim) {
//            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            @Suppress("DEPRECATION")
//            if (AndroidUtil.isICSOrLater())
//                navbar = navbar or View.SYSTEM_UI_FLAG_LOW_PROFILE
//            else
//                visibility = visibility or View.STATUS_BAR_HIDDEN
//            if (!AndroidDevices.hasCombBar()) {
//                navbar = navbar or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                if (AndroidUtil.isKitKatOrLater())
//                    visibility = visibility or View.SYSTEM_UI_FLAG_IMMERSIVE
//                if (AndroidUtil.isJellyBeanOrLater())
//                    visibility = visibility or View.SYSTEM_UI_FLAG_FULLSCREEN
//            }
//        } else {
//            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            @Suppress("DEPRECATION")
//            if (AndroidUtil.isICSOrLater())
//                visibility = visibility or View.SYSTEM_UI_FLAG_VISIBLE
//            else
//                visibility = visibility or View.STATUS_BAR_VISIBLE
//        }
//
//        if (AndroidDevices.hasNavBar())
//            visibility = visibility or navbar
//        activity?.window?.decorView?.systemUiVisibility = visibility
//    }
//
//    private fun showController(needShow: Boolean, isInfiniteShow: Boolean = false) {
//        if (needShow) {
//            controller?.visibility = View.VISIBLE
//            hideNavigationBar(false)
//
//            if (isEndReached || isInfiniteShow || !(mediaPlayer?.isPlaying ?: false)) {
//                autoHideController(-1)
//            } else {
//                autoHideController()
//            }
//            isControllerVisible = true
//        } else {
//            controller?.visibility = View.GONE
//            hideNavigationBar(true)
//            isControllerVisible = false
//        }
//    }
//
//    @Subscribe
//    fun onIncomingCall(event: IncomingCallEvent) {
//        pausePlayer()
//    }
//
//    fun initPhoneStateListener() {
//        try {
//            tmgr?.listen(myStatePhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
//        } catch (ex: Exception) {
//            analytic.reportError(Analytic.Error.INIT_PHONE_STATE, ex)
//        }
//    }
//
//    fun removePhoneStateCallbacks() {
//        try {
//            tmgr?.listen(myStatePhoneListener, PhoneStateListener.LISTEN_NONE)
//        } catch(ex: Exception) {
//            analytic.reportError(Analytic.Error.REMOVE_PHONE_STATE, ex)
//        }
//    }
//
//    @Subscribe
//    fun onAudioFocusLoss(event: AudioFocusLossEvent) {
//        pausePlayer()
//    }
//
//    fun startLoading() {
//        analytic.reportEvent(Analytic.Video.START_LOADING)
//        isLoading = true
//        showController(false)
//        progressBar?.visibility = View.VISIBLE
//    }
//
//    fun stopLoading() {
//        analytic.reportEvent(Analytic.Video.STOP_LOADING)
//        progressBar?.visibility = View.GONE
//        showController(true)
//        isLoading = false
//    }
}