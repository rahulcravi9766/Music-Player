package com.example.tabbuttons.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.system.Os
import android.telephony.TelephonyManager
import android.util.Log
import android.util.StateSet
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tabbuttons.R
import com.example.tabbuttons.fragments.MusicPlayerFragment
import com.example.tabbuttons.model.*
import java.lang.Exception
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {
    private lateinit var runnable: Runnable

 override fun onReceive(context: Context?, intent: Intent?) {

     val state = intent!!.getStringExtra(TelephonyManager.EXTRA_STATE)
   when(intent.action){
    Application.PREVIOUS -> nextPreviousButtonsMusic(increment = false,context = context!!)
    Application.PLAY -> if(musicService!!.mediaPlayer!!.isPlaying)pauseMusic() else playMusic()
    Application.NEXT -> nextPreviousButtonsMusic(increment = true,context = context!!)
    Application.EXIT -> {
        musicService!!.stopForeground(true)
        musicService = null
        exitProcess(1)
    }
   }

     try {
         when {
             state.equals(TelephonyManager.EXTRA_STATE_RINGING) -> {
                 pauseMusic()
             }
             state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) -> {
                 pauseMusic()
             }
             state.equals(TelephonyManager.EXTRA_STATE_IDLE) -> {
                 playMusic()
             }
         }

     } catch (ex: Exception) {
     }
 }
    //ok
    private fun playMusic(){
        musicService!!.mediaPlayer!!.isPlaying
        musicService!!.musicFragment.musicBinding.playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        musicService!!.tabFragment.tabBinding.playPauseButtonBottom.setImageResource(R.drawable.ic_pause_bottom)
        musicService!!.mediaPlayer!!.start()
        musicService!!.showNotification(R.drawable.ic_pause_notification,1F)
        Log.i("sktest","play")
    }

    //0k
    private fun pauseMusic(){
        !musicService!!.mediaPlayer!!.isPlaying
        musicService!!.musicFragment.musicBinding.playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        musicService!!.tabFragment.tabBinding.playPauseButtonBottom.setImageResource(R.drawable.ic_play_bottom)
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24,0F)
        Log.i("sktest","pause")
    }


    private fun nextPreviousButtonsMusic(increment: Boolean,context: Context){
        Log.i("Execution","previous mp in notification")
    musicService!!.setSongPosition(increment = increment)
        musicService!!.createMediaPlayer()
        Glide.with(context).load(musicService!!.listOfSongs[musicService!!.songPosition].songPhoto)
            .apply(RequestOptions().placeholder(R.drawable.ic_baseline_music_video_24).centerCrop())
            .into(musicService!!.musicFragment.musicBinding.imageView)

        //adding song name to player
        val songName = musicService!!.listOfSongs[musicService!!.songPosition].songName
        musicService!!.musicFragment.musicBinding.songNameId.text = songName
        musicService!!.musicFragment.musicBinding.songNameId.isSelected=true
        musicService!!.tabFragment.tabBinding.songNameBottom.text = songName
        playMusic()
    }

//    private fun seekBarMovement() {
//        runnable = Runnable {
//            musicService!!.musicFragment.musicBinding.starTime.text =
//                toMinutes(musicService!!.mediaPlayer!!.currentPosition.toLong())
//            musicService!!.musicFragment.musicBinding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
//            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
//        }
//        Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
//    }
//    private fun seeking(){
//        musicService!!.musicFragment.musicBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
//
//            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
//        })
//    }



}