package com.example.tabbuttons.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tabbuttons.R
import com.example.tabbuttons.adapter.PlaylistAdapter
import com.example.tabbuttons.adapter.SongListAdapter
import com.example.tabbuttons.dataBase.SongDao
import com.example.tabbuttons.dataBase.SongDatabase
import com.example.tabbuttons.databinding.FragmentPlaylistBinding
import com.example.tabbuttons.model.musicService
import com.example.tabbuttons.model.playlistList
import com.example.tabbuttons.model.songModel
import com.example.tabbuttons.service.PlayMusicService
import kotlinx.coroutines.*


class PlaylistFragment(
    view: View

    ) : Fragment() , ServiceConnection{

    lateinit var playListBinding: FragmentPlaylistBinding
    lateinit var playlistRecyclerView: RecyclerView
    lateinit var playAdapter : PlaylistAdapter
    private lateinit var songDao: SongDao
    private val views = view
    var navController = Navigation.findNavController(views)



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val intent = Intent(activity, PlayMusicService::class.java)
        activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        activity?.startService(intent)

        songDao = SongDatabase.getDatabase(requireActivity().application).songDao()
        playListBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false)
        playlistRecyclerView = playListBinding.playlistRecyclerView
       // playlistRecyclerView.isNestedScrollingEnabled = false
        playListBinding.favoriteCardView.setOnClickListener {
            val actions = TabFragmentDirections.actionTabFragmentToFavFragment()
            navController.navigate(actions)
        }

//        playListBinding.favoriteCardView.setOnLongClickListener {
//
//        }

        return playListBinding.root
    }

    override fun onResume() {
        super.onResume()
       readPlaylistNames()

    }

    private fun readPlaylistNames(){

            GlobalScope.launch(Dispatchers.IO) {
                musicService!!.readPlaylistNamesFromDatabase(requireActivity())
                withContext(Dispatchers.Main){
                    playlistRecyclerView()

            }
        }
    }



    private fun playlistRecyclerView() {
        playlistRecyclerView.apply {
            playAdapter = activity?.let { PlaylistAdapter(playlistList,playListBinding.root,it) }!!

            adapter = playAdapter
            Log.i("recyclerPlay", "check playList")
            layoutManager = GridLayoutManager(activity,2)
        }
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as PlayMusicService.MyBinder
        musicService = binder.currentService()
        readPlaylistNames()
        musicService!!.playListView = playListBinding.root
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
       musicService = null
    }
}