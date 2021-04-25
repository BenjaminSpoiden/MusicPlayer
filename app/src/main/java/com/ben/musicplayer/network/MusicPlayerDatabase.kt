package com.ben.musicplayer.network

import android.util.Log
import com.ben.musicplayer.model.Song
import com.ben.musicplayer.utils.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.isActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class MusicPlayerDatabase @Inject constructor(firestore: FirebaseFirestore) {
    private val songCollection = firestore.collection(SONG_COLLECTION)

    suspend fun getSongs(): List<Song> {
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            Log.d("Tag", "${e.message}")
            emptyList()
        }
    }
}