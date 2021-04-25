package com.ben.musicplayer.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ben.musicplayer.model.Song

abstract class BaseSongAdapter(
    private val layoutId: Int
): RecyclerView.Adapter<BaseSongAdapter.SongViewHolder>() {


    protected var onSongItemClickListener: ((Song) -> Unit)? = null

    class SongViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    protected val differCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }
    }

    protected abstract val differ: AsyncListDiffer<Song>

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    fun setOnItemClickListener(listener: (Song) -> Unit) {
        onSongItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                layoutId,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}