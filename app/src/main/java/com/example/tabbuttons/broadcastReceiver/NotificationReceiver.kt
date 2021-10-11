package com.example.tabbuttons.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tabbuttons.R
import com.example.tabbuttons.model.*
import java.lang.Exception
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {

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
    }

    //0k
    private fun pauseMusic(){
        !musicService!!.mediaPlayer!!.isPlaying
        musicService!!.musicFragment.musicBinding.playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        musicService!!.tabFragment.tabBinding.playPauseButtonBottom.setImageResource(R.drawable.ic_play_bottom)
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24,0F)
    }


    private fun nextPreviousButtonsMusic(increment: Boolean,context: Context){
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
}