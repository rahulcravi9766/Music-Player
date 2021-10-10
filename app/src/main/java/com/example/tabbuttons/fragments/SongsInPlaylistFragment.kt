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
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tabbuttons.R
import com.example.tabbuttons.adapter.PlaylistAdapter
import com.example.tabbuttons.adapter.SongListAdapter
import com.example.tabbuttons.dataBase.SongDao
import com.example.tabbuttons.dataBase.SongDatabase
import com.example.tabbuttons.databinding.FragmentSongsInPlaylistBinding
import com.example.tabbuttons.model.musicService
import com.example.tabbuttons.model.songModel
import com.example.tabbuttons.service.PlayMusicService
import com.trendyol.bubblescrollbarlib.BubbleTextProvider
import kotlinx.android.synthetic.main.fragment_songs_in_playlist.view.*
import kotlinx.coroutines.*
import java.lang.StringBuilder

class SongsInPlaylistFragment : Fragment(), ServiceConnection {


    lateinit var songsInPlaylistBinding: FragmentSongsInPlaylistBinding
    private lateinit var songDao: SongDao
    lateinit var songInPlaylistRv: RecyclerView
    lateinit var songsInPlaylistAdapter: SongListAdapter
    private lateinit var navController : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val intent = Intent(activity, PlayMusicService::class.java)
        activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        activity?.startService(intent)
        songsInPlaylistBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_songs_in_playlist, container, false)
        songInPlaylistRv = songsInPlaylistBinding.songsInPlaylistRv
        navController = findNavController()


//        songsInPlaylistBinding.bubbleScrollBarInPlaylist.attachToRecyclerView(songInPlaylistRv)
//        songsInPlaylistBinding.bubbleScrollBarInPlaylist.bubbleTextProvider = BubbleTextProvider {
//                position ->
//            StringBuilder(musicService!!.songsInsidePlaylist[position].songName).toString()
//        }
        setHasOptionsMenu(true)

      //  mainview=songsInPlaylistBinding.root
        songDao = SongDatabase.getDatabase(requireActivity().application).songDao()


        return songsInPlaylistBinding.root
    }

    private val onClicked : (songModel, Int)->Unit={ songModel: songModel, i: Int ->

        Log.i("onclicked",i.toString())
        val actions = SongsInPlaylistFragmentDirections.actionSongsInPlaylistFragmentToMusicPlayerFragment(

            i,songModel.songId
        )
        Toast.makeText(requireActivity(),"hey", Toast.LENGTH_SHORT).show()//position : position of a single song when clicked on the list, listOfSongs[position].songId : id of the same song is passed to the next fragment
        navController.navigate(actions)

    }



//    private fun readPlaylistSongs() {
//        musicService!!.readSongsInPlaylist()
//        songsInPlaylistRecyclerView()
//    }

    private fun songsInPlaylistRecyclerView() {

        songInPlaylistRv.apply {
            songsInPlaylistAdapter =
                activity?.let {
                    SongListAdapter(
                        musicService!!.songsInsidePlaylist as MutableList<songModel>,onClicked,songsInPlaylistBinding.root, 3,
                        it
                    )
                }!!
            adapter = songsInPlaylistAdapter
            Log.i("recyclerPlay", "check playList")

            layoutManager = LinearLayoutManager(activity)

        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as PlayMusicService.MyBinder
        musicService = binder.currentService()

      songsInPlaylistRecyclerView()


    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}