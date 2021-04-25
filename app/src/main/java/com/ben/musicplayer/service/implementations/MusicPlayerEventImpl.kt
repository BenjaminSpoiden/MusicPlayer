package com.ben.musicplayer.service.implementations

import android.util.Log
import android.widget.Toast
import com.ben.musicplayer.service.MusicService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventImpl(
    private val musicService: MusicService
): Player.EventListener {

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if(playbackState == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Log.d("Tag", "error: ${error.message}")
        Toast.makeText(musicService, "An error occurred", Toast.LENGTH_LONG).show()
    }
}