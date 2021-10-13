package com.rahul_music_pod.tabbuttons.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.rahul_music_pod.tabbuttons.R
import com.rahul_music_pod.tabbuttons.model.musicService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*

class PlaylistAdapter(
    var playlistList: MutableList<String>,
    fragmentContext: FragmentActivity

) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    var context = fragmentContext

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val playlistName = itemView.findViewById<TextView>(R.id.playlist_name)!!
        val playListCard = itemView.findViewById<CardView>(R.id.playlist_card_view)!!

    }

    private fun deleteBox(adapterPosition: Int): Boolean {
        val builder = MaterialAlertDialogBuilder(context)
        Log.i("hello","working")
        builder.setTitle("Delete Playlist?")
            .setMessage("Do you want to delete this playlist?")
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

               this@PlaylistAdapter.notifyDataSetChanged()
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
               playlistName.text = playlistList[position]

           playListCard.setOnClickListener {
                GlobalScope.launch (Dispatchers.IO){
                    musicService!!.readSongsInPlaylist(playlistList[position])
                    withContext(Dispatchers.Main){
                        musicService!!.playlistViewInService()
                    }
                }
            }
           playListCard.setOnLongClickListener {
               deleteBox(position)
            }
        }
    }


    override fun getItemCount(): Int {
        return playlistList.size
    }
}


