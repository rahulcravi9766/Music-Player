package com.rahul_music_pod.tabbuttons.fragments

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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rahul_music_pod.tabbuttons.R
import com.rahul_music_pod.tabbuttons.adapter.SongListAdapter
import com.rahul_music_pod.tabbuttons.databinding.FragmentHomeBinding
import com.rahul_music_pod.tabbuttons.model.musicService
import com.rahul_music_pod.tabbuttons.service.PlayMusicService
import com.trendyol.bubblescrollbarlib.BubbleTextProvider
import android.provider.Settings
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.rahul_music_pod.tabbuttons.model.SongModel

class HomeFragment(private val mainView: View) : Fragment(), ServiceConnection {

    var listOfSongs = mutableListOf<SongModel>()

    private lateinit var homeRecyclerView: RecyclerView
    lateinit var homeAdapter: SongListAdapter
    lateinit var homeBinding: FragmentHomeBinding
    private lateinit var navController: NavController



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        homeRecyclerView = homeBinding.homeRecyclerView
        navController = Navigation.findNavController(mainView)
        permission.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
            )
        )

             //bubble sorting
        homeBinding.bubbleScrollBar.attachToRecyclerView(homeRecyclerView)
        homeBinding.bubbleScrollBar.bubbleTextProvider = BubbleTextProvider {
                position ->
            StringBuilder(listOfSongs[position].songName.substring(0,1)).toString()
        }

        return homeBinding.root
    }

    private val permission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            if (!it[Manifest.permission.READ_EXTERNAL_STORAGE]!! && !it[Manifest.permission.READ_PHONE_STATE]!!) {
                showMessage(3)
            } else if (!it[Manifest.permission.READ_EXTERNAL_STORAGE]!!) {
                showMessage(1)
            } else if (!it[Manifest.permission.READ_PHONE_STATE]!!) {
                val intent = Intent(activity, PlayMusicService::class.java)
                activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
                activity?.startService(intent)

                loadData()
                showMessage(2)
            } else {
                val intent = Intent(activity, PlayMusicService::class.java)
                activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
                activity?.startService(intent)

                loadData()
            }

        }
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.i("PermissionBack", "test")
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(activity, PlayMusicService::class.java)
                activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
                activity?.startService(intent)

                loadData()
            } else {
                showMessage(1)
            }

        }

    private fun showMessage(perValue: Int) {
        val message: String
        if (perValue == 1) {

            message = "Read media file permission is required to read all the songs"
        } else if (perValue == 2) {

            message =
                "Phone state permission may be required in order to control the music system while calling"
        } else {

            message = "Permission is actually necessary to work the application"
        }
        AlertDialog.Builder(requireActivity())
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->
                if (perValue != 2) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                    resultLauncher.launch(intent)
                } else {
                    dialog.dismiss()
                }
            }
            .create()
            .show()
    }

    private val onClicked: (SongModel, Int) -> Unit = { songModel: SongModel, i: Int ->
        val actions = TabFragmentDirections.actionTabFragmentToMusicPlayerFragment(
            i, songModel.songId
        )
        //position : position of a single song when clicked on the list, listOfSongs[position].songId : id of the same song is passed to the next fragment
        navController.navigate(actions)
    }

    private fun loadData() {

        listOfSongs.clear()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val songCursor: Cursor? = activity?.contentResolver?.query(
            uri, projection,
            null, null, MediaStore.Audio.Media.TITLE
        )

        if (songCursor != null) {
            while (songCursor.moveToNext()) {
                val id = songCursor.getInt(0)
                val path = songCursor.getString(1)
                val title = songCursor.getString(2)
                val time = songCursor.getLong(3)
                val songPhoto = songCursor.getLong(5)
                val timeStampId = System.currentTimeMillis()
                val timeStamp = timeStampId.toString() + id.toString()
                val uri = Uri.parse("content://media/external/audio/albumart")
                val artUri = Uri.withAppendedPath(uri, songPhoto.toString()).toString()
                listOfSongs.add(SongModel(timeStamp, id, "favorite", title, time, path, artUri))
            }
        } else {
            Toast.makeText(context, "Unable to process", Toast.LENGTH_SHORT).show()
        }
        homeRecyclerView.apply {
            homeAdapter =
                activity?.let { SongListAdapter(listOfSongs, onClicked,  1, it) }!!
            adapter = homeAdapter
            layoutManager = LinearLayoutManager(activity)
            // homeBinding.fastscroll.setRecyclerView(homeRecyclerView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.more_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_ascending -> {
                sortSongListArray(true)
            }
            R.id.sort_descending -> {
                sortSongListArray(false)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun sortSongListArray(clicked: Boolean) {
        val ascendingSongs = mutableListOf<SongModel>()
        val descendingSongs = mutableListOf<SongModel>()
        if (clicked) {
            if (ascendingSongs.isNotEmpty()) ascendingSongs.clear()
            ascendingSongs.addAll(listOfSongs)
            ascendingSongs.sortBy {
                it.songName
            }
            musicService!!.listOfSongs = ascendingSongs
            homeRecyclerView.apply {
                homeAdapter =
                    activity?.let { SongListAdapter(ascendingSongs, onClicked,  1, it) }!!
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(activity)

            }
        } else {
            if (descendingSongs.isNotEmpty()) ascendingSongs.clear()
            descendingSongs.addAll(listOfSongs)
            descendingSongs.sortByDescending {
                it.songName
            }
            musicService!!.listOfSongs = descendingSongs
            homeRecyclerView.apply {
                homeAdapter =
                    activity?.let { SongListAdapter(descendingSongs, onClicked,  1, it) }!!
                adapter = homeAdapter
                layoutManager = LinearLayoutManager(activity)
            }
        }
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as PlayMusicService.MyBinder
        musicService = binder.currentService()
        musicService!!.songListView = homeBinding.root
        musicService!!.allSongs = listOfSongs
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}