package com.ben.musicplayer.ui.adapter

import androidx.recyclerview.widget.AsyncListDiffer
import com.ben.musicplayer.R
import com.ben.musicplayer.model.Song

class SwipeSongAdapter: BaseSongAdapter(R.layout.swipe_song) {
    override val differ: AsyncListDiffer<Song> = AsyncListDiffer(this, differCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.itemView.apply {

        }
    }
}