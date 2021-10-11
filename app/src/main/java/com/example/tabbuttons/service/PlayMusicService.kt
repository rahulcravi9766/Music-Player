package com.example.tabbuttons.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tabbuttons.broadcastReceiver.Application
import com.example.tabbuttons.activity.MainActivity
import com.example.tabbuttons.R
import com.example.tabbuttons.fragments.MusicPlayerFragment
import com.example.tabbuttons.model.*
import java.lang.Exception
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.media.app.NotificationCompat
import androidx.navigation.Navigation
import com.example.tabbuttons.broadcastReceiver.NotificationReceiver
import com.example.tabbuttons.fragments.TabFragment
import com.example.tabbuttons.dataBase.SongDatabase
import com.example.tabbuttons.fragments.TabFragmentDirections
import kotlinx.coroutines.*
import java.lang.Runnable
import java.lang.ref.WeakReference
import java.util.*


class PlayMusicService : Service() {

    private lateinit var runnable: Runnable
    private lateinit var mediaSession: MediaSessionCompat
    private var myBinder = MyBinder()
    lateinit var musicFragment: WeakReference<MusicPlayerFragment>
    lateinit var mainActivity: WeakReference<MainActivity>
    lateinit var tabFragment: WeakReference<TabFragment>
    lateinit var listOfSongs: List<SongModel>
    lateinit var allSongs: List<SongModel>
    lateinit var favSongs: List<SongModel>
    lateinit var favMusicList: List<SongModel>
    lateinit var insidePlaylistList: List<SongModel>
    lateinit var songsInsidePlaylist: List<SongModel>
    lateinit var playListView: View
    lateinit var favView: View
    lateinit var songListView: View
    var songPosition = -1
    var currentSongId = -1
    var sleepButton = false
    var mediaPlayer: MediaPlayer? = null


    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }


    inner class MyBinder : Binder() {
        fun currentService(): PlayMusicService {
            return this@PlayMusicService
        }
    }

    fun alteringListOfSongs(value: Int) {
        when (value) {
            1 -> {
                listOfSongs = emptyList()
                listOfSongs = allSongs
            }
            2 -> {
                listOfSongs = emptyList()
                listOfSongs = favSongs
            }
            3 -> {
                listOfSongs = emptyList()
                listOfSongs = insidePlaylistList
            }
//            4 -> {
//                listOfSongs = listOfSongs.shuffled()
//
//            }
        }
    }

    fun readPlaylistNamesFromDatabase(act: FragmentActivity) {
        val songDao = SongDatabase.getDatabase(act.application).songDao()
        GlobalScope.launch(Dispatchers.IO) {
            playlistList = songDao.readDistinctNames() as MutableList<String>
            if (playlistList.contains("favorite")) {
                playlistList.remove("favorite")
            }
        }
    }

    fun deleteAllSongsOfPlaylist(adapterPosition: Int) {
        val songDao = SongDatabase.getDatabase(application).songDao()
        songDao.deleteAllSongs(playlistList[adapterPosition])
        playlistList.removeAt(adapterPosition)
    }


    fun playlistViewInService() {
        val navController = Navigation.findNavController(playListView)
        val actions = TabFragmentDirections.actionTabFragmentToSongsInPlaylistFragment()
        navController.navigate(actions)
    }

    fun readFavSongs(favorite: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val SongDao = SongDatabase.getDatabase(application).songDao()
            favMusicList = SongDao.readAllData(favorite)
            favSongs = favMusicList
        }
    }

    fun readSongsInPlaylist(name: String) {

        GlobalScope.launch(Dispatchers.IO) {
            val SongDao = SongDatabase.getDatabase(application).songDao()
            insidePlaylistList = SongDao.readAllSongsFromPlaylist(name)
            withContext(Dispatchers.Main) {
                songsInsidePlaylist = emptyList()
                songsInsidePlaylist = insidePlaylistList
            }
        }
    }

    fun tabCalling(activity: TabFragment) {
        val weakTabFragment=WeakReference(activity)
        this.tabFragment = weakTabFragment
    }

    fun seekBarMovement(activity: MusicPlayerFragment) {
        val weakMusicPlayerFragment = WeakReference(activity)
        this.musicFragment = weakMusicPlayerFragment
        runnable = Runnable {
            musicFragment.get().let { it?.musicBinding?.starTime?.text =
                musicService!!.mediaPlayer!!.currentPosition.toLong() .let { it1 ->
                    toMinutes(
                        it1
                    )
                }
            }

            musicFragment.get().let { it?.musicBinding?.seekBar?.progress =
                musicService!!.mediaPlayer!!.currentPosition }
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
    }

    fun showNotification(playPauseBtn: Int, playbackSpeed: Float) {
        val bundle = Bundle()
        bundle.putInt("selectedSongPosition", songPosition)
        bundle.putInt("selectedSongId", currentSongId)


        //to open the application from the notification
        val pendingIntent = NavDeepLinkBuilder(baseContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.home_to_player)
            .setDestination(R.id.musicPlayerFragment)
            .setArguments(bundle)
            .createPendingIntent()

        val previousIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(Application.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(Application.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(Application.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val closeIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(Application.EXIT)
        val closePendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            closeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val imgArt = getImgArt(listOfSongs[songPosition].songPath)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.diskimg)
        }

        val notification =
            androidx.core.app.NotificationCompat.Builder(baseContext, Application.CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(listOfSongs[songPosition].songName)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setLargeIcon(image)
                .setStyle(
                    NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                        .setMediaSession(mediaSession.sessionToken)
                )
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_previous_notification, "Previous", prevPendingIntent)
                .addAction(playPauseBtn, "Play", playPendingIntent)
                .addAction(R.drawable.ic_next_notification, "Next", nextPendingIntent)
                .addAction(R.drawable.ic_close, "Exit", closePendingIntent)
                .build()


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            //setting duration in seekBar
//            mediaSession.setMetadata(
//                mediaPlayer.get().let { it?.duration?.toLong() }?.let {
//                    MediaMetadataCompat.Builder()
//                        .putLong(
//                            MediaMetadataCompat.METADATA_KEY_DURATION,
//                            it
//                        )
//                        .build()
//                }
//            )
//            mediaSession.setPlaybackState(
//                PlaybackStateCompat.Builder().setState(
//                    PlaybackStateCompat.STATE_PLAYING,
//                    mediaPlayer.get().let { it?.currentPosition?.toLong() }!!,
//                    playbackSpeed
//                )
//                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                    .build()
//            )
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            //setting duration in seekBar
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                .build())
            mediaSession.setPlaybackState(PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING,mediaPlayer!!.currentPosition.toLong(),playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build())
        }

        mediaSession.setCallback(object: MediaSessionCompat.Callback(){
            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                mediaPlayer!!.seekTo(pos.toInt())

                val playBackStateNew = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
                mediaSession.setPlaybackState(playBackStateNew)
            }
        })

        startForeground(13, notification)
    }

    fun createMediaPlayer() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }

            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(listOfSongs[songPosition].songPath)
            mediaPlayer!!.prepare()
            musicFragment.get().let { it?.musicBinding?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24) }
              // musicService!!.showNotification(R.drawable.ic_pause_notification,1F)
            musicFragment.get().let { it?.musicBinding?.starTime?.text =
                toMinutes(musicService!!.mediaPlayer!!.currentPosition.toLong())}

            musicFragment.get().let { it?.musicBinding?.endTime?.text =
                toMinutes(musicService!!.mediaPlayer!!.duration.toLong()) }
            musicFragment.get().let { it?.musicBinding?.seekBar?.progress = 0 }
            musicFragment.get().let { it?.musicBinding?.seekBar?.max = musicService!!.mediaPlayer!!.duration }

        } catch (e: Exception) {
            return
        }
    }

    //ok
    fun playMusic() {
        musicFragment.get().let { it?.musicBinding?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24) }
        musicService!!.showNotification(R.drawable.ic_pause_notification, 1F)
        tabFragment.get().let {
            it?.tabBinding?.playPauseButtonBottom?.setImageResource(R.drawable.ic_pause_bottom)
        }
        musicService!!.mediaPlayer!!.start()
        currentSongId = musicService!!.listOfSongs[songPosition].songId
    }


    //ok
    fun pauseMusic() {
        musicFragment.get().let { it?.musicBinding?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_filled_24) }
        musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24, 0F)
        tabFragment.get().let {
            it?.tabBinding?.playPauseButtonBottom?.setImageResource(R.drawable.ic_play_bottom)
        }
        musicService!!.mediaPlayer!!.pause()
    }

    fun nextPreviousButtons(increment: Boolean) {
        musicFragment.get().let { it?.musicBinding?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24) }
        if (increment) {
            musicService!!.setSongPosition(increment = true)
            createMediaPlayer()
            playMusic()
            setLayout()
        } else {
            musicService!!.setSongPosition(increment = false)
            createMediaPlayer()
            playMusic()
            setLayout()
        }
    }

    fun setLayout() {

        val songName = musicService!!.listOfSongs[musicService!!.songPosition].songName
        musicFragment.get().let { it?.musicBinding?.songNameId?.isSelected = true }
        musicFragment.get().let { it?.musicBinding?.songNameId?.text = songName }

        //adding song photo to music player
        musicFragment.get().let {
            if (it != null) {
                musicFragment.get().let { it?.musicBinding?.imageView }?.let { it1 ->
                    Glide.with(it).load(musicService!!.listOfSongs[musicService!!.songPosition].songPhoto)
                        .apply(
                            RequestOptions().placeholder(R.drawable.diskimg).centerCrop()
                        )
                        .into(it1)
                }
            }
        }
    }

    fun setSongPosition(increment: Boolean) {
        if (increment) {
            if (listOfSongs.size - 1 == songPosition)
            else ++songPosition
        } else {
            if (0 == songPosition)
            else --songPosition
        }
    }
}