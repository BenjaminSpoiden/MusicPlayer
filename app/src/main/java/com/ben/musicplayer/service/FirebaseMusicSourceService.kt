package com.ben.musicplayer.service

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.ben.musicplayer.network.MusicPlayerDatabase
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSourceService @Inject constructor(
    private val musicPlayerDatabase: MusicPlayerDatabase
) {

    private val readyListener: MutableList<(Boolean) -> Unit> = mutableListOf()

    private var state: ServiceState = ServiceState.CREATED
        set(value) {
            if(value == ServiceState.INITIALIZED || value == ServiceState.FAILED) {
                synchronized(readyListener) {
                    field = value
                    readyListener.forEach {
                        it(state == ServiceState.INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun onReady(action: (Boolean) -> Unit): Boolean {
        return if(state == ServiceState.CREATED || state == ServiceState.INITIALIZING) {
            readyListener.add(action)
            false
        } else {
            action(state == ServiceState.INITIALIZED)
            true
        }
    }

    var songs: List<MediaMetadataCompat> = emptyList()

    suspend fun onFetchMediaMetadata() = withContext(Dispatchers.IO) {
        state = ServiceState.INITIALIZING
        val allSongs = musicPlayerDatabase.getSongs()
        songs = allSongs.map { song ->
            Builder()
                .putString(METADATA_KEY_ARTIST, song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .build()
        }
        state = ServiceState.INITIALIZED
    }

    fun mediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach {
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(it.getString(METADATA_KEY_MEDIA_URI).toUri())

            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        return concatenatingMediaSource
    }

    fun mediaItems() = songs.map { song ->
        val description = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()

        MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE)
    }.toMutableList()
}