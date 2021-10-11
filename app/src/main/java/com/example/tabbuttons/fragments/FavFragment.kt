package com.example.tabbuttons.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tabbuttons.R
import com.example.tabbuttons.databinding.FragmentFavBinding
import com.example.tabbuttons.adapter.SongListAdapter
import com.example.tabbuttons.model.SongModel
import com.example.tabbuttons.model.musicService
import com.example.tabbuttons.service.PlayMusicService

class FavFragment : Fragment(), ServiceConnection {
    lateinit var adapterMusic: SongListAdapter
    lateinit var favListBinding: FragmentFavBinding
    lateinit var recyclerView: RecyclerView
    private lateinit var navController : NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)
        val intent = Intent(activity, PlayMusicService::class.java)
        activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        activity?.startService(intent)

        favListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_fav, container, false)
        navController=  findNavController()
        recyclerView = favListBinding.favRecyclerView

        if(musicService!!.favMusicList.isEmpty()){
            favListBinding.textInFav.visibility = View.VISIBLE
        }

//        favListBinding.bubbleScrollBarFav.attachToRecyclerView(recyclerView)
//        favListBinding.bubbleScrollBarFav.bubbleTextProvider = BubbleTextProvider {
//                position ->
//            StringBuilder(musicService!!.listOfSongs[position].songName).toString()
//        }

        return favListBinding.root
    }

    private fun readAllSongs() {
        musicService!!.readFavSongs("favorite")
        favRecycleView()
    }


    private val onClicked : (SongModel, Int)->Unit={ songModel: SongModel, i: Int ->
        val actions = FavFragmentDirections.actionFavFragmentToMusicPlayerFragment(
            i,songModel.songId
        )
        //position : position of a single song when clicked on the list, listOfSongs[position].songId : id of the same song is passed to the next fragment
        navController.navigate(actions)
    }

    private fun favRecycleView() {
        recyclerView.apply {
            adapterMusic =
                activity?.let {
                    SongListAdapter(
                        musicService!!.favSongs as MutableList<SongModel>,onClicked,  2,
                        it
                    )
                }!!
            adapter = adapterMusic
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onResume() {
        super.onResume()
        readAllSongs()
    }

    //for creating buttons in action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as PlayMusicService.MyBinder
        musicService = binder.currentService()
        musicService!!.favView = favListBinding.root
        readAllSongs()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}