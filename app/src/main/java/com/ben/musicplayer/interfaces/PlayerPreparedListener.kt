package com.ben.musicplayer.interfaces

import android.support.v4.media.MediaMetadataCompat

interface PlayerPreparedListener {
    fun onPlayerPrepared(mediaMetadataCompat: MediaMetadataCompat?)
}