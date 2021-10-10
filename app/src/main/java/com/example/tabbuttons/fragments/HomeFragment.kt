package com.example.tabbuttons.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tabbuttons.R
import com.example.tabbuttons.adapter.SongListAdapter
import com.example.tabbuttons.databinding.FragmentHomeBinding
import com.example.tabbuttons.model.musicService
import com.example.tabbuttons.model.songModel
import com.example.tabbuttons.service.PlayMusicService
import com.trendyol.bubblescrollbarlib.BubbleScrollBar
import com.trendyol.bubblescrollbarlib.BubbleTextProvider
import kotlinx.android.synthetic.main.fragment_home.*
import java.lang.StringBuilder
import java.util.*
import android.os.Build
import android.provider.Settings
import android.view.*
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tabbuttons.adapter.PlaylistAdapter
import com.example.tabbuttons.databinding.ActivityMainBinding.inflate
import com.example.tabbuttons.model.playlistList
import java.util.zip.Inflater


class HomeFragment(private val mainView: View) : Fragment(), ServiceConnection {

    var listOfSongs = mutableListOf<songModel>()

    //private val views = mainView
    lateinit var homeRecyclerView: RecyclerView
    lateinit var homeAdapter: SongListAdapter
    lateinit var homeBinding: FragmentHomeBinding
    private lateinit var navController:NavController

    //  val scrollBar = itemView.findViewById<BubbleScrollBar>(R.id.bubbleScrollBar)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        homeRecyclerView = homeBinding.homeRecyclerView
        navController=Navigation.findNavController(mainView)
        permission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE))

        //bubble sorting
        homeBinding.bubbleScrollingBar.attachToRecyclerView(homeRecyclerView)
        homeBinding.bubbleScrollingBar.bubbleTextProvider = BubbleTextProvider {
                position ->
            StringBuilder(listOfSongs[position].songName.substring(0,1)).toString()
        }

        return homeBinding.root
    }

    private val permission=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

        if (!it[Manifest.permission.READ_EXTERNAL_STORAGE]!! && !it[Manifest.permission.READ_PHONE_STATE]!!){
            showMessage(3)
        }else if(!it[Manifest.permission.READ_EXTERNAL_STORAGE]!!){
            showMessage(1)
        }else if (!it[Manifest.permission.READ_PHONE_STATE]!!){
            val intent = Intent(activity, PlayMusicService::class.java)
            activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
            activity?.startService(intent)

            loadData()
            showMessage(2)
        }else{
            val intent = Intent(activity, PlayMusicService::class.java)
            activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
            activity?.startService(intent)

            loadData()
        }

    }
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.i("PermissionBack","test")
        if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            val intent = Intent(activity, PlayMusicService::class.java)
            activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
            activity?.startService(intent)

            loadData()
        }else{
            showMessage(1)
        }

    }
    private fun showMessage(perValue:Int)
    {
        val message: String

        if(perValue==1){

            message="Read media file permission is required to read all the songs"
        }else if (perValue==2){

            message="Phone state permission may be required in order to control the music system while calling"
        }else{

            message="Permission is actually necessary to work the application"
        }
        AlertDialog.Builder(requireActivity())
            .setMessage(message)
            .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, _ ->
                if (perValue!=2){
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package",requireActivity().packageName, null)
                    intent.data = uri
                    resultLauncher.launch(intent)
                }
                else{
                    dialog.dismiss()
                }

            })
            .create()
            .show()
    }

    private val onClicked : (songModel, Int)->Unit={ songModel: songModel, i: Int ->

        Log.i("onClicked",i.toString())
        val actions = TabFragmentDirections.actionTabFragmentToMusicPlayerFragment(

            i,songModel.songId
        )
    Toast.makeText(requireActivity(),"hey",Toast.LENGTH_SHORT).show()//position : position of a single song when clicked on the list, listOfSongs[position].songId : id of the same song is passed to the next fragment
        navController.navigate(actions)

    }

    private fun loadData() {
        Log.i("Execution","fun called")
        listOfSongs.clear()
       Log.i("Execution","loading data")
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val songCursor: Cursor? = activity?.contentResolver?.query(               //check
            uri, projection,
            null, null, MediaStore.Audio.Media.TITLE
        )

        if (songCursor != null) {
        Log.i("Permission","if working")
            while (songCursor.moveToNext()) {
                val id = songCursor.getInt(0)
                val path = songCursor.getString(1)
                val title = songCursor.getString(2)
                val time = songCursor.getLong(3)   //todo
                val songPhoto = songCursor.getLong(5)
                val timeStampId = System.currentTimeMillis()
                val timeStamp = timeStampId.toString()+id.toString()
                val uri = Uri.parse("content://media/external/audio/albumart")
                val artUri = Uri.withAppendedPath(uri, songPhoto.toString()).toString()
                listOfSongs.add(songModel(timeStamp,id, "favorite", title, time, path, artUri))
            }
        } else {
            Log.i("Permission","else working")
            Toast.makeText(context, "Unable to process", Toast.LENGTH_SHORT).show()
        }
        homeRecyclerView.apply {
            homeAdapter = activity?.let { SongListAdapter(listOfSongs,onClicked, mainView,  1,it) }!!
            adapter = homeAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

//    private fun playlistRecyclerView() {
//        playlistRecyclerView.apply {
//            playAdapter = activity?.let { PlaylistAdapter(playlistList,playListBinding.root,it) }!!
//
//            adapter = playAdapter
//            Log.i("recyclerPlay", "check playList")
//            layoutManager = GridLayoutManager(activity,2)
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
      //  super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.more_menu,menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.sort_ascending -> {
                Log.i("test","asced")
                sortSongListArray(true)

              //  Toast.makeText(this,"working",Toast.LENGTH_SHORT).show()
            }
            R.id.sort_descending -> {
                Log.i("test","desce")
                sortSongListArray(false)
              //  Toast.makeText(this,"working",Toast.LENGTH_SHORT).show()
            }
        }


        return super.onOptionsItemSelected(item)
    }



    private fun sortSongListArray(clicked: Boolean){
        val ascendingSongs = mutableListOf<songModel>()
        val descendingSongs = mutableListOf<songModel>()
        if(clicked){
            if (ascendingSongs.isNotEmpty())ascendingSongs.clear()
            ascendingSongs.addAll(listOfSongs)
            ascendingSongs.sortBy {
                it.songName
            }
                musicService!!.listOfSongs = ascendingSongs
                homeRecyclerView.apply {
                    homeAdapter = activity?.let { SongListAdapter(ascendingSongs,onClicked, mainView,  1,it) }!!
                    adapter = homeAdapter
                    layoutManager = LinearLayoutManager(activity)
                   for(i in ascendingSongs){
                       Log.i("test", ascendingSongs.toString())
                   }
            }
        }else{
            if (descendingSongs.isNotEmpty())ascendingSongs.clear()
            descendingSongs.addAll(listOfSongs)
            descendingSongs.sortByDescending {
                it.songName}
            for(i in descendingSongs){
                Log.i("testDec", i.songName)
            }
                musicService!!.listOfSongs = descendingSongs
                homeRecyclerView.apply {
                    homeAdapter = activity?.let { SongListAdapter(descendingSongs,onClicked, mainView,  1,it) }!!
                    adapter = homeAdapter
                    layoutManager = LinearLayoutManager(activity)
                    for(i in descendingSongs){
                        Log.i("test",descendingSongs.toString())
                    }
                }
        }
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {

        val binder = service as PlayMusicService.MyBinder
        musicService = binder.currentService()
        musicService!!.songListView = homeBinding.root
        musicService!!.allSongs = listOfSongs
        Log.i("Execution", "services Started")
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}