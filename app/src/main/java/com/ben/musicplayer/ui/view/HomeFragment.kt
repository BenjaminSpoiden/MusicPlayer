package com.ben.musicplayer.ui.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ben.musicplayer.R
import com.ben.musicplayer.network.ResponseStatus
import com.ben.musicplayer.ui.adapter.SongAdapter
import com.ben.musicplayer.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel
    @Inject lateinit var songAdapter: SongAdapter

    private lateinit var songRecyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        songRecyclerView = view.findViewById(R.id.songRecyclerView)
        setupRecyclerView()
        onSubscribeToObservers()

        songAdapter.setOnItemClickListener  {
            mainViewModel.playOrToggleSong(it)
        }
    }

    private fun setupRecyclerView() = songRecyclerView.apply {
        this.adapter = songAdapter
        this.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun onSubscribeToObservers() {
        mainViewModel.mediaItem.observe(viewLifecycleOwner) {
            when(it) {
                is ResponseStatus.Successful -> {
                    it.response.let { songs ->
                        songAdapter.songs = songs
                    }
                }
                is ResponseStatus.Failed -> {
                    Log.d("Tag", "Failed: ${it.message}")
                }
                is ResponseStatus.Loading -> {
                    Log.d("Tag", "Loading...")
                }
            }
        }
    }
}