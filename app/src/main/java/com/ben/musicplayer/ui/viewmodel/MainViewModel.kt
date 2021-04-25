package com.ben.musicplayer.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ben.musicplayer.model.Song
import com.ben.musicplayer.network.ResponseStatus
import com.ben.musicplayer.service.MusicServiceConnection
import com.ben.musicplayer.utils.MEDIA_ROOT_ID
import com.ben.musicplayer.utils.isPlayEnabled
import com.ben.musicplayer.utils.isPlaying
import com.ben.musicplayer.utils.isPrepared
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
): ViewModel() {

    private val _mediaItems = MutableLiveData<ResponseStatus<List<Song>>>()
    val mediaItem: LiveData<ResponseStatus<List<Song>>> get() = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentlyPlayingSong = musicServiceConnection.currentlyPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(ResponseStatus.Loading)
        musicServiceConnection.onSubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString(),
                        it.mediaId!!
                    )
                }
                _mediaItems.postValue(ResponseStatus.Successful(items))
            }
        })
    }

    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(song: Song, toggle: Boolean = false) {
        val isPlayerPrepared = playbackState.value?.isPrepared ?: false
        if(isPlayerPrepared && song.mediaId == currentlyPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackStateCompat ->
                when {
                    playbackStateCompat.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                    playbackStateCompat.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(song.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.onUnsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}