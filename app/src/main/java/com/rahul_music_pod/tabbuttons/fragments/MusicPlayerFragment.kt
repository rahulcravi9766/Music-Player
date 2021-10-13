package com.rahul_music_pod.tabbuttons.fragments

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.*
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.rahul_music_pod.tabbuttons.R
import com.rahul_music_pod.tabbuttons.dataBase.SongDao
import com.rahul_music_pod.tabbuttons.dataBase.SongDatabase
import com.rahul_music_pod.tabbuttons.databinding.FragmentMusicPlayerBinding
import com.rahul_music_pod.tabbuttons.model.*
import com.rahul_music_pod.tabbuttons.service.PlayMusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.Exception

class MusicPlayerFragment : Fragment(), ServiceConnection, MediaPlayer.OnCompletionListener {

    private val args: MusicPlayerFragmentArgs by navArgs()
    lateinit var musicBinding: FragmentMusicPlayerBinding
    private lateinit var songDao: SongDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {

        //for setting options in action bar
        setHasOptionsMenu(true)
        songDao = SongDatabase.getDatabase(requireActivity().application).songDao()
        musicBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_music_player, container, false)

        if (musicService!!.mediaPlayer != null && musicService!!.currentSongId == args.selectedSongId) {
            musicService!!.songPosition = args.selectedSongPosition
            if (musicService!!.mediaPlayer!!.isPlaying) musicService!!.showNotification(
                R.drawable.ic_pause_bottom,
                1F
            )
            else musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24, 0F)
            addingAndRemovingFavSong()
            checkPlayPauseButton()
            musicBinding.endTime.text =
                toMinutes(musicService!!.mediaPlayer!!.duration.toLong())   //musicService!!.songPosition = position of the song in the music player .If we change the song from the music player the position that we passed through the navigation of song list adapter will be different. ex: Song position in the music player is 2 and the adapter is 1. So if we go back from player and click the same song it should not start from the first, it should resume.
            musicService!!.seekBarMovement(this)
            musicService!!.setLayout()
            musicBinding.seekBar.max = musicService!!.mediaPlayer!!.duration
        } else {
            val intent = Intent(activity, PlayMusicService::class.java)
            activity?.bindService(intent, this, BIND_AUTO_CREATE)
            activity?.startService(intent)
        }

        // play pause functions
        musicBinding.playPauseButton.setOnClickListener {
            if (musicService!!.mediaPlayer!!.isPlaying) {
                musicService!!.pauseMusic()
            } else musicService!!.playMusic()
        }

        //previous and next button
        musicBinding.previousButton.setOnClickListener {
            musicService!!.nextPreviousButtons(increment = false)
            addingAndRemovingFavSong()
        }

        musicBinding.nextButton.setOnClickListener {
            musicService!!.nextPreviousButtons(increment = true)
            addingAndRemovingFavSong()
        }

        //favorite
        musicBinding.favoriteButton.setOnClickListener {
            checkingFavSongForAddingAndRemoving()
        }

        musicBinding.shuffleButton.setOnClickListener {
           musicService!!.listOfSongs.shuffled()
            Toast.makeText(context, "Shuffled", Toast.LENGTH_SHORT).show()
        }

        musicBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                    if (musicService!!.mediaPlayer!!.isPlaying) musicService!!.showNotification(
                        R.drawable.ic_pause_bottom,
                        1F
                    )
                    else musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24, 0F)

                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })

        return musicBinding.root
    }

    private fun checkPlayPauseButton() {
        if (musicService!!.mediaPlayer!!.isPlaying) {
            musicBinding.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24)
        } else {
            musicBinding.playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        }
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }

            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(
                musicService!!.listOfSongs[musicService!!.songPosition].songPath
            )

            musicService!!.mediaPlayer!!.prepare()    //decoding the song data
            musicService!!.mediaPlayer!!.start()
            musicService!!.currentSongId =
                musicService!!.listOfSongs[musicService!!.songPosition].songId

            musicBinding.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24)
            musicBinding.starTime.text =
                toMinutes(musicService!!.mediaPlayer!!.currentPosition.toLong())
            musicBinding.endTime.text = toMinutes(musicService!!.mediaPlayer!!.duration.toLong())
            musicBinding.seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)   //this : the current class
            addingAndRemovingFavSong()
            musicService!!.showNotification(R.drawable.ic_pause_notification, 1F)  //ok
        } catch (e: Exception) {
            return
        }
    }

    private fun addToFavorite() {
        musicBinding.favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_24)
        System.currentTimeMillis()
        CoroutineScope(Dispatchers.IO).launch {
            songDao.addSong(musicService!!.listOfSongs[musicService!!.songPosition])
            withContext(Dispatchers.Main) {
                musicService!!.favSongs = emptyList()
                musicService!!.readFavSongs("favorite")
            }

        }
    }

    private fun removeFromFavorite() {
        musicBinding.favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        CoroutineScope(Dispatchers.IO).launch {
            songDao.removeSong(musicService!!.listOfSongs[musicService!!.songPosition])
            musicService!!.favSongs = emptyList()
            musicService!!.readFavSongs("favorite")
        }
    }

    private fun addingAndRemovingFavSong(): Boolean {
        for (songs in musicService!!.favSongs) {
            if (songs.songId == musicService!!.currentSongId) {
                musicBinding.favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_24)
                return true
            }
        }
        musicBinding.favoriteButton.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        return false
    }


    private fun checkingFavSongForAddingAndRemoving() {
        val value = addingAndRemovingFavSong()
        if (value) {
            removeFromFavorite()
        } else {
            addToFavorite()
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    //for creating buttons in action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

    //service will be connected
    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as PlayMusicService.MyBinder
        musicService = binder.currentService()
        musicService!!.seekBarMovement(this)
        musicService!!.songPosition = args.selectedSongPosition
        musicService!!.setLayout()
        createMediaPlayer()

    }

    // service will be stopped
    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    //playing the next song automatically after the current song
    override fun onCompletion(p0: MediaPlayer?) {
        musicService!!.setSongPosition(increment = true)
        createMediaPlayer()

        try {
            musicService!!.setLayout()
            musicBinding.endTime.text = toMinutes(musicService!!.mediaPlayer!!.duration.toLong())
        } catch (e: Exception) {
            return
        }
    }
}