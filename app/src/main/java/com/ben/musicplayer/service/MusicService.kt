package com.ben.musicplayer.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.ben.musicplayer.interfaces.PlayerPreparedListener
import com.ben.musicplayer.interfaces.SongCallbackListener
import com.ben.musicplayer.service.implementations.MusicPlaybackPreparerImpl
import com.ben.musicplayer.service.implementations.MusicPlayerEventImpl
import com.ben.musicplayer.service.implementations.MusicPlayerNotificationImpl
import com.ben.musicplayer.utils.MEDIA_ROOT_ID
import com.ben.musicplayer.utils.NETWORK_ERROR
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MusicService @Inject constructor(
    private val dataSourceFactory: DefaultDataSourceFactory,
    private val exoPlayer: SimpleExoPlayer,
    private val firebaseMusicSourceService: FirebaseMusicSourceService
) : MediaBrowserServiceCompat(), SongCallbackListener, PlayerPreparedListener {

    companion object {
        private const val SERVICE_TAG = "service_tag"
        var currentSongDuration = 0L
            private set
    }

    private lateinit var musicNotificationService: MusicNotificationService

    private val musicServiceCoroutineJob = Job()
    private val musicServiceCoroutineScope = CoroutineScope(Dispatchers.Main + musicServiceCoroutineJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var musicPlayerEventImpl: MusicPlayerEventImpl

    var isForegroundService = false
    private var isPlayerInitialized = false

    private var currentlyPlayingSong: MediaMetadataCompat? = null

    override fun onCreate() {
        super.onCreate()
        musicServiceCoroutineScope.launch {
            firebaseMusicSourceService.onFetchMediaMetadata()
        }

        val pendingIntent = packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            PendingIntent.getActivity(this, 0, intent, 0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            this.setSessionActivity(pendingIntent)
            this.isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationService = MusicNotificationService(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationImpl(this),
            this
        )

        val musicPlaybackPreparerImpl = MusicPlaybackPreparerImpl(firebaseMusicSourceService, this)

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparerImpl)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator(mediaSession, firebaseMusicSourceService))
        mediaSessionConnector.setPlayer(exoPlayer)
        musicNotificationService.onShowNotification(exoPlayer)
        musicPlayerEventImpl = MusicPlayerEventImpl(this)
        exoPlayer.addListener(musicPlayerEventImpl)
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        playableItem: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val currentSongIndex = if(currentlyPlayingSong == null) 0 else songs.indexOf(playableItem)
        exoPlayer.prepare(firebaseMusicSourceService.mediaSource(dataSourceFactory))
        exoPlayer.seekTo(currentSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
       when(parentId) {
           MEDIA_ROOT_ID -> {
               val resultSent = firebaseMusicSourceService.onReady { isReady ->
                   if(isReady) {
                       result.sendResult(firebaseMusicSourceService.mediaItem().toMutableList())
                       if(!isPlayerInitialized && firebaseMusicSourceService.songs.isNotEmpty()) {
                            preparePlayer(firebaseMusicSourceService.songs, firebaseMusicSourceService.songs[0], false)
                            isPlayerInitialized = true
                       }
                   } else {
                       mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                       result.sendResult(null) //NetworkError
                   }
               }
               if(!resultSent) {
                   result.detach()
               }
           }
       }
    }

    override fun onNewSongCallbackListener() {
        currentSongDuration = exoPlayer.duration
    }

    override fun onPlayerPrepared(mediaMetadataCompat: MediaMetadataCompat?) {
        currentlyPlayingSong = mediaMetadataCompat
        preparePlayer(
            firebaseMusicSourceService.songs,
            mediaMetadataCompat,
            true
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        musicServiceCoroutineScope.cancel()
        exoPlayer.removeListener(musicPlayerEventImpl)
        exoPlayer.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }
}