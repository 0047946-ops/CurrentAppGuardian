package com.guardianapp.videoplayer

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.ui.PlayerView
import com.guardianapp.videoplayer.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var playbackStateManager: PlaybackStateManager
    private lateinit var playbackErrorHandler: PlaybackErrorHandler
    private lateinit var smartBufferingManager: SmartBufferingManager
    private lateinit var gestureDetector: GestureDetector

    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnBackward: ImageButton
    private lateinit var btnFullscreen: ImageButton
    private lateinit var btnQuality: ImageButton
    private lateinit var btnSpeed: ImageButton
    private lateinit var btnYouTube: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvQualityDisplay: TextView
    private lateinit var tvSpeedDisplay: TextView
    private lateinit var progressBuffering: ProgressBar

    private var isFullScreen = false
    private var currentQuality = "auto"
    private var currentSpeed = 1.0f
    private var isSeeking = false
    private var progressUpdateJob: kotlinx.coroutines.Job? = null

    // Gesture variables
    private var lastTouchY = 0f
    private var lastTouchX = 0f
    private val gestureThreshold = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        initializePlayer()
        initializeNetworkMonitoring()
        initializeGestureDetector()
        loadLastPlaybackState()
        startProgressUpdate()
    }

    private fun initializeViews() {
        playerView = findViewById(R.id.playerView)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnForward = findViewById(R.id.btnForward)
        btnBackward = findViewById(R.id.btnBackward)
        btnFullscreen = findViewById(R.id.btnFullscreen)
        btnQuality = findViewById(R.id.btnQuality)
        btnSpeed = findViewById(R.id.btnSpeed)
        btnYouTube = findViewById(R.id.btnYouTube)
        seekBar = findViewById(R.id.seekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvDuration = findViewById(R.id.tvDuration)
        progressBuffering = findViewById(R.id.progressBuffering)

        // Create text views for quality and speed display
        tvQualityDisplay = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            text = "Auto"
            visibility = View.GONE
        }
        tvSpeedDisplay = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            text = "1.0x"
            visibility = View.GONE
        }

        setupClickListeners()
        setupSeekBar()
        setupTouchListener()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setBufferingParameters(
                androidx.media3.exoplayer.LoadControl.DEFAULT.bufferingParameters
                    .buildUpon()
                    .setTargetBufferBytes(20 * 1000 * 1000) // 20MB
                    .setMaxBufferMs(30000) // 30 seconds max
                    .setMinBufferMs(2500)  // 2.5 seconds min
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()
            )
            .build()
            .apply {
                // Enable hardware decoding priority
                setPreferredAudioLanguage("en")
                playerView.player = this
                addListener(playerListener)
                addAnalyticsListener(EventLogger("ExoPlayer"))
            }

        playbackStateManager = PlaybackStateManager(this)
        networkMonitor = NetworkMonitor(this)
        playbackErrorHandler = PlaybackErrorHandler(player!!, networkMonitor, lifecycleScope)
        smartBufferingManager = SmartBufferingManager(player!!)
    }

    private fun initializeNetworkMonitoring() {
        networkMonitor.startMonitoring()

        lifecycleScope.launch {
            networkMonitor.networkState.collect { state ->
                handleNetworkStateChange(state)
            }
        }
    }

    private fun initializeGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // Double tap to toggle play/pause
                if (player?.isPlaying == true) {
                    player?.pause()
                } else {
                    player?.play()
                }
                return true
            }
        })
    }

    private fun setupTouchListener() {
        playerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchY = event.y
                    lastTouchX = event.x
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.y - lastTouchY
                    val deltaX = event.x - lastTouchX

                    // Vertical swipe on left side = brightness, right side = volume
                    if (abs(deltaY) > gestureThreshold) {
                        if (event.x < playerView.width / 2) {
                            // Left side - brightness
                            handleBrightnessGesture(deltaY)
                        } else {
                            // Right side - volume
                            handleVolumeGesture(deltaY)
                        }
                        lastTouchY = event.y
                    }
                }
                MotionEvent.ACTION_UP -> {}
            }
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun handleVolumeGesture(deltaY: Float) {
        val audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)

        // Swipe up = increase volume, swipe down = decrease volume
        val newVolume = if (deltaY < 0) {
            (currentVolume + 1).coerceAtMost(maxVolume)
        } else {
            (currentVolume - 1).coerceAtLeast(0)
        }

        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVolume, 0)
    }

    private fun handleBrightnessGesture(deltaY: Float) {
        val window = window
        val params = window.attributes
        var brightness = params.screenBrightness

        if (brightness < 0) brightness = 0.5f // Default to 50%

        // Swipe up = increase brightness, swipe down = decrease brightness
        brightness = if (deltaY < 0) {
            (brightness + 0.05f).coerceAtMost(1f)
        } else {
            (brightness - 0.05f).coerceAtLeast(0.1f)
        }

        params.screenBrightness = brightness
        window.attributes = params
    }

    private fun handleNetworkStateChange(state: NetworkState) {
        when {
            state.isConnected -> {
                if (player?.isPlaying == false) {
                    player?.prepare()
                }
                playbackErrorHandler.resetRetryCount()
            }
            else -> {
                player?.pause()
            }
        }
    }

    private fun loadLastPlaybackState() {
        lifecycleScope.launch {
            playbackStateManager.getPlaybackState().collect { state ->
                if (state.videoUrl.isNotEmpty()) {
                    playVideo(state.videoUrl)
                    player?.seekTo(state.playbackPositionMs)
                    player?.setPlaybackSpeed(state.playbackSpeed)
                    currentQuality = state.selectedQuality
                    currentSpeed = state.playbackSpeed
                } else {
                    // Default to YouTube
                    openYouTubeFirst()
                }
            }
        }
    }

    private fun openYouTubeFirst() {
        startActivity(Intent(this, YouTubeActivity::class.java))
    }

    private fun playVideo(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
        }
    }

    private fun setupClickListeners() {
        btnPlayPause.setOnClickListener {
            if (player?.isPlaying == true) {
                player?.pause()
            } else {
                player?.play()
            }
        }

        btnForward.setOnClickListener {
            player?.seekTo((player?.currentPosition ?: 0L) + 10000)
        }

        btnBackward.setOnClickListener {
            player?.seekTo(((player?.currentPosition ?: 0L) - 10000).coerceAtLeast(0L))
        }

        btnFullscreen.setOnClickListener {
            toggleFullscreen()
        }

        btnQuality.setOnClickListener {
            showQualitySelector()
        }

        btnSpeed.setOnClickListener {
            showSpeedSelector()
        }

        btnYouTube.setOnClickListener {
            startActivity(Intent(this, YouTubeActivity::class.java))
        }
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && !isSeeking) {
                    player?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isSeeking = false
                player?.seekTo(seekBar.progress.toLong())
            }
        })
    }

    private fun toggleFullscreen() {
        isFullScreen = !isFullScreen
        playerView.apply {
            layoutParams = FrameLayout.LayoutParams(
                if (isFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else FrameLayout.LayoutParams.MATCH_PARENT,
                if (isFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else 300
            )
        }
    }

    private fun showQualitySelector() {
        val qualities = arrayOf("Auto (Adaptive)", "1080p", "720p", "480p", "360p")
        val qualityValues = arrayOf("auto", "1080p", "720p", "480p", "360p")
        val selectedIndex = qualityValues.indexOf(currentQuality)

        AlertDialog.Builder(this)
            .setTitle("Select Quality")
            .setSingleChoiceItems(qualities, selectedIndex) { dialog, which ->
                currentQuality = qualityValues[which]
                updateQualityDisplay()
                applyQualitySettings(currentQuality)
                dialog.dismiss()
            }
            .show()
    }

    private fun applyQualitySettings(quality: String) {
        // Quality adjustment logic - simulated
        // In production, you would adjust track selection based on quality
        when (quality) {
            "1080p" -> {
                smartBufferingManager.updateBufferingStrategy(10f, 5f) // 10 Mbps network, 5 Mbps video
            }
            "720p" -> {
                smartBufferingManager.updateBufferingStrategy(5f, 2.5f)
            }
            "480p" -> {
                smartBufferingManager.updateBufferingStrategy(2.5f, 1.5f)
            }
            "360p" -> {
                smartBufferingManager.updateBufferingStrategy(1.5f, 0.8f)
            }
            else -> {
                // Auto - use network monitor
                val bandwidth = networkMonitor.getEstimatedBandwidthMbps()
                smartBufferingManager.updateBufferingStrategy(bandwidth, 2.5f)
            }
        }
    }

    private fun updateQualityDisplay() {
        tvQualityDisplay.apply {
            text = currentQuality.uppercase()
            visibility = View.VISIBLE
            alpha = 1f
        }
        lifecycleScope.launch {
            delay(2000)
            tvQualityDisplay.visibility = View.GONE
        }
    }

    private fun showSpeedSelector() {
        val speeds = arrayOf("0.5×", "0.75×", "1.0×", "1.25×", "1.5×", "2.0×")
        val speedValues = floatArrayOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val selectedIndex = speedValues.indexOf(currentSpeed)

        AlertDialog.Builder(this)
            .setTitle("Select Speed")
            .setSingleChoiceItems(speeds, selectedIndex) { dialog, which ->
                currentSpeed = speedValues[which]
                player?.setPlaybackSpeed(currentSpeed)
                updateSpeedDisplay()
                dialog.dismiss()
            }
            .show()
    }

    private fun updateSpeedDisplay() {
        tvSpeedDisplay.apply {
            text = String.format("%.2f×", currentSpeed)
            visibility = View.VISIBLE
            alpha = 1f
        }
        lifecycleScope.launch {
            delay(2000)
            tvSpeedDisplay.visibility = View.GONE
        }
    }

    private fun startProgressUpdate() {
        progressUpdateJob = lifecycleScope.launch {
            while (true) {
                delay(500) // Update every 500ms
                if (!isSeeking) {
                    updateUIState()
                }
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            updateUIState()
        }

        override fun onPlaybackStateChanged(state: Int) {
            updateUIState()
            smartBufferingManager.onPlayerStateChanged(
                player?.playWhenReady ?: false,
                state
            )
            when (state) {
                Player.STATE_BUFFERING -> progressBuffering.visibility = View.VISIBLE
                else -> progressBuffering.visibility = View.GONE
            }
        }

        override fun onPlayerError(error: com.guardianapp.videoplayer.core.PlaybackException) {
            playbackErrorHandler.handlePlaybackError(error)
        }
    }

    private fun updateUIState() {
        val isPlaying = player?.isPlaying ?: false
        btnPlayPause.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )

        val duration = player?.duration ?: 0L
        val position = player?.currentPosition ?: 0L

        if (duration > 0) {
            seekBar.max = duration.toInt()
        }
        if (!isSeeking) {
            seekBar.progress = position.toInt()
        }

        tvCurrentTime.text = formatTime(position)
        tvDuration.text = formatTime(duration)
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = ms / (1000 * 60 * 60)
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        player?.pause()
        lifecycleScope.launch {
            playbackStateManager.savePlaybackState(
                PlaybackState(
                    videoUrl = player?.currentMediaItem?.localConfiguration?.uri.toString(),
                    playbackPositionMs = player?.currentPosition ?: 0L,
                    playbackSpeed = player?.playbackParameters?.speed ?: 1f,
                    selectedQuality = currentQuality,
                    volume = player?.volume ?: 1f
                )
            )
        }
        super.onPause()
    }

    override fun onDestroy() {
        progressUpdateJob?.cancel()
        networkMonitor.stopMonitoring()
        player?.release()
        super.onDestroy()
    }
}
