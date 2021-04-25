package com.ben.musicplayer.service

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ben.musicplayer.network.Event
import com.ben.musicplayer.network.ResponseStatus
import com.ben.musicplayer.utils.NETWORK_ERROR
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

class MusicServiceConnection(
   @ApplicationContext context: Context
) {
    private val _isConnected: MutableLiveData<Event<ResponseStatus<Boolean>>> = MutableLiveData()
    val isConnected: LiveData<Event<ResponseStatus<Boolean>>> get() = _isConnected

    private val _networkError: MutableLiveData<Event<ResponseStatus<Boolean>>> = MutableLiveData()
    val networkError: LiveData<Event<ResponseStatus<Boolean>>> get() = _networkError

    private val _playbackState: MutableLiveData<PlaybackStateCompat?> = MutableLiveData()
    val playbackState: LiveData<PlaybackStateCompat?> get() = _playbackState

    private val _currentlyPlayingSong: MutableLiveData<MediaMetadataCompat?> = MutableLiveData()
    val currentlyPlayingSong: LiveData<MediaMetadataCompat?> get() = _currentlyPlayingSong


    lateinit var mediaController: MediaControllerCompat
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaControllerCallback = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun onSubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaControllerCallback.subscribe(parentId, callback)
    }

    fun onUnsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaControllerCallback.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context): MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaControllerCallback.sessionToken).apply {
                this.registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(Event(ResponseStatus.Successful(true)))
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(Event(ResponseStatus.Failed("The connection was suspended.")))
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(Event(ResponseStatus.Failed("Couldn't connect to the media Browser.")))
        }
    }

    private inner class MediaControllerCallback: MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentlyPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            when(event) {
                NETWORK_ERROR -> {
                    _networkError.postValue(Event(ResponseStatus.Failed("Couldn't connect to the server, check your internet connection.")))
                }
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}