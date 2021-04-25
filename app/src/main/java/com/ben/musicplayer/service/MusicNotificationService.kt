package com.ben.musicplayer.service

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.ben.musicplayer.R
import com.ben.musicplayer.interfaces.SongCallbackListener
import com.ben.musicplayer.utils.NOTIFICATION_CHANNEL_ID
import com.ben.musicplayer.utils.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext

class MusicNotificationService (
   @ApplicationContext private val context: Context,
   sessionToken: MediaSessionCompat.Token,
   notificationListener: PlayerNotificationManager.NotificationListener,
   private val songCallbackListener: SongCallbackListener
) {
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)
        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_name,
            R.string.notification_desc,
            NOTIFICATION_ID,
            DescriptionAdapterService(mediaController),
            notificationListener
        ).apply {
            this.setSmallIcon(R.drawable.exo_notification_small_icon)
            this.setMediaSessionToken(sessionToken)
        }
    }

    private inner class DescriptionAdapterService(private val mediaControllerCompat: MediaControllerCompat): PlayerNotificationManager.MediaDescriptionAdapter {

        override fun createCurrentContentIntent(player: Player): PendingIntent? = mediaControllerCompat.sessionActivity

        override fun getCurrentContentText(player: Player): CharSequence = mediaControllerCompat.metadata.description.subtitle.toString()

        override fun getCurrentContentTitle(player: Player): CharSequence = mediaControllerCompat.metadata.description.title.toString()

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            TODO("Not yet implemented")
        }
    }

}