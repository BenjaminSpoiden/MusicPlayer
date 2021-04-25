package com.ben.musicplayer.service.implementations

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import com.ben.musicplayer.interfaces.PlayerPreparedListener
import com.ben.musicplayer.service.FirebaseMusicSourceService
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class MusicPlaybackPreparerImpl(
    private val firebaseMusicSourceService: FirebaseMusicSourceService,
    private val playerPreparedListener: PlayerPreparedListener
): MediaSessionConnector.PlaybackPreparer {

    override fun onCommand(
        player: Player,
        controlDispatcher: ControlDispatcher,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ): Boolean = false

    override fun getSupportedPrepareActions(): Long =
        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

    override fun onPrepare(playWhenReady: Boolean) = Unit

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        firebaseMusicSourceService.onReady {
            fun findPlayableSong() = firebaseMusicSourceService.songs.find { song -> mediaId == song.description.mediaId }
            playerPreparedListener.onPlayerPrepared(findPlayableSong())
        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
}