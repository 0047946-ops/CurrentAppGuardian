package com.guardianapp.videoplayer.core

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class BackgroundPlaybackService : Service() {
    private var player: ExoPlayer? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BackgroundPlaybackService = this@BackgroundPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val videoUrl = intent?.getStringExtra("VIDEO_URL") ?: ""
        if (videoUrl.isNotEmpty()) {
            playInBackground(videoUrl)
        }
        return START_STICKY
    }

    private fun playInBackground(videoUrl: String) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun pausePlayback() {
        player?.pause()
    }

    fun resumePlayback() {
        player?.play()
    }

    fun stopPlayback() {
        player?.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        player?.release()
        super.onDestroy()
    }
}
