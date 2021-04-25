package com.ben.musicplayer.ui.adapter

import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import com.ben.musicplayer.R
import com.ben.musicplayer.model.Song
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
): BaseSongAdapter(R.layout.song_item) {

    override val differ: AsyncListDiffer<Song> = AsyncListDiffer(this, differCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            this.findViewById<TextView>(R.id.songTitle).text = song.title
            setOnClickListener {
                onSongItemClickListener?.let { clickListener ->
                    clickListener(song)
                }
            }
        }
    }

}