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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.guardianapp.videoplayer.core.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var playbackStateManager: PlaybackStateManager
    private lateinit var playbackErrorHandler: PlaybackErrorHandler
    private lateinit var smartBufferingManager: SmartBufferingManager

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
    private lateinit var progressBuffering: ProgressBar

    private var isFullScreen = false
    private var currentQuality = "auto"
    private var currentSpeed = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        initializePlayer()
        initializeNetworkMonitoring()
        loadLastPlaybackState()
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

        setupClickListeners()
        setupSeekBar()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setBufferingParameters(
                androidx.media3.exoplayer.LoadControl.DEFAULT.bufferingParameters
                    .buildUpon()
                    .setTargetBufferBytes(20 * 1000 * 1000) // 20MB
                    .build()
            )
            .build()
            .apply {
                playerView.player = this
                addListener(playerListener)
            }

        playbackStateManager = PlaybackStateManager(this)
        playbackErrorHandler = PlaybackErrorHandler(player!!, networkMonitor, lifecycleScope)
        smartBufferingManager = SmartBufferingManager(player!!)
    }

    private fun initializeNetworkMonitoring() {
        networkMonitor = NetworkMonitor(this)
        networkMonitor.startMonitoring()

        lifecycleScope.launch {
            networkMonitor.networkState.collect { state ->
                handleNetworkStateChange(state)
            }
        }
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
            player?.seekTo(player?.currentPosition!! + 10000)
        }

        btnBackward.setOnClickListener {
            player?.seekTo(player?.currentPosition!! - 10000)
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
                if (fromUser) {
                    player?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun toggleFullscreen() {
        isFullScreen = !isFullScreen
        playerView.apply {
            layoutParams = FrameLayout.LayoutParams(
                if (isFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else FrameLayout.LayoutParams.MATCH_PARENT,
                if (isFullScreen) FrameLayout.LayoutParams.MATCH_PARENT else FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun showQualitySelector() {
        // Implement quality selection dialog
        val qualities = arrayOf("Auto", "1080p", "720p", "480p", "360p")
    }

    private fun showSpeedSelector() {
        // Implement speed selection dialog
        val speeds = arrayOf("0.5x", "0.75x", "1.0x", "1.25x", "1.5x", "2.0x")
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

        override fun onPlayerError(error: PlaybackException) {
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

        seekBar.max = duration.toInt()
        seekBar.progress = position.toInt()

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
        networkMonitor.stopMonitoring()
        player?.release()
        super.onDestroy()
    }
}
