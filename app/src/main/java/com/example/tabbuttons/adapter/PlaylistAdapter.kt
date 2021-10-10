package com.example.tabbuttons.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.tabbuttons.R
import com.example.tabbuttons.dataBase.SongDao
import com.example.tabbuttons.fragments.PlaylistFragment
import com.example.tabbuttons.model.musicService
import com.example.tabbuttons.model.songModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*

class PlaylistAdapter(
    var playlistList: MutableList<String>,
    view: View,
    fragmentContext: FragmentActivity


) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {


    var context = fragmentContext
    private lateinit var songDao: SongDao


    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val playlistName = itemView.findViewById<TextView>(R.id.playlist_name)!!
        val playListCard = itemView.findViewById<CardView>(R.id.playlist_card_view)!!

    }

    private fun deleteBox(adapterPosition: Int): Boolean {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Remove Song")
            .setMessage("Do you want to remove this song from playlist?")
            .setPositiveButton("Yes") { _, _ ->
                removingPlaylist(adapterPosition)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()

            }
        val customDialog = builder.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)

        return true
    }

    private fun removingPlaylist(adapterPosition: Int){
       GlobalScope.launch (Dispatchers.IO){
           musicService!!.deleteAllSongsOfPlaylist(adapterPosition)
           withContext(Dispatchers.Main){
               this@PlaylistAdapter.notifyItemRemoved(adapterPosition)
           }
       }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.playlist_recycler_view, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.apply {
//edited on oct6 holder.itemView
               playlistName.text = playlistList[position]
            //    Log.i("Position", playlistList[position].toString())
        //    Toast.makeText(context,"hello",Toast.LENGTH_SHORT).show()

            holder.playListCard.setOnClickListener {
                GlobalScope.launch (Dispatchers.IO){
                    musicService!!.readSongsInPlaylist(playlistList[position])
                    withContext(Dispatchers.Main){
                        musicService!!.playlistViewInService()
                    }
                }
            }
            holder.playListCard.setOnLongClickListener {
               deleteBox(position)
            }
        }
    }


    override fun getItemCount(): Int {
        return playlistList.size
    }
}


