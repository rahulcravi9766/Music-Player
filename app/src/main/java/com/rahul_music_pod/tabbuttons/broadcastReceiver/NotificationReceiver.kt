package com.rahul_music_pod.tabbuttons.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rahul_music_pod.tabbuttons.R
import com.rahul_music_pod.tabbuttons.model.*
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
                 if (musicService!!.mediaPlayer != null){
                     pauseMusic()
                 }

             }
             state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) -> {
                 if (musicService!!.mediaPlayer != null){
                     pauseMusic()
                 }
             }
             state.equals(TelephonyManager.EXTRA_STATE_IDLE) -> {
                 if (musicService!!.mediaPlayer != null){
                     playMusic()
                 }
             }
         }

     } catch (ex: Exception) {
     }
 }
    //ok
    private fun playMusic(){
        musicService!!.mediaPlayer!!.isPlaying
        musicService!!.musicFragment.get().let { it?.musicBinding?.playPauseButton?.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24) }
        musicService!!.tabFragment.get().let { it?.tabBinding?.playPauseButtonBottom?.setImageResource(R.drawable.ic_pause_bottom) }
        musicService!!.mediaPlayer!!.start()
        musicService!!.showNotification(R.drawable.ic_pause_notification,1F)
    }

    //0k
    private fun pauseMusic(){
        !musicService!!.mediaPlayer!!.isPlaying
        musicService!!.musicFragment.get().let { it?.musicBinding?.playPauseButton?.setImageResource(R.drawable.ic_baseline_play_circle_filled_24) }
        musicService!!.tabFragment.get().let { it?.tabBinding?.playPauseButtonBottom?.setImageResource(R.drawable.ic_play_bottom)}
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24,0F)
    }


    private fun nextPreviousButtonsMusic(increment: Boolean,context: Context){
    musicService!!.setSongPosition(increment = increment)
        musicService!!.createMediaPlayer()
        musicService!!.musicFragment.get().let { it?.musicBinding?.imageView }?.let {
            Glide.with(context).load(musicService!!.listOfSongs[musicService!!.songPosition].songPhoto)
                .apply(RequestOptions().placeholder(R.drawable.diskimg).centerCrop())
                .into(it)
        }

        //adding song name to player
        val songName = musicService!!.listOfSongs[musicService!!.songPosition].songName
        musicService!!.musicFragment.get().let { it?.musicBinding?.songNameId?.text = songName }
        musicService!!.musicFragment.get().let { it?.musicBinding?.songNameId?.isSelected=true }
        musicService!!.tabFragment.get().let { it?.tabBinding?.songNameBottom?.text = songName}
        playMusic()
    }
}