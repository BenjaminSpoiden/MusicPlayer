package com.ben.musicplayer.service

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

class MusicQueueNavigator(
    private val mediaSessionCompat: MediaSessionCompat,
    private val firebaseMusicSourceService: FirebaseMusicSourceService
): TimelineQueueNavigator(mediaSessionCompat) {

    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
        return firebaseMusicSourceService.songs[windowIndex].description
    }
}