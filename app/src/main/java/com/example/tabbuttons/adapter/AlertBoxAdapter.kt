package com.example.tabbuttons.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tabbuttons.R
import com.example.tabbuttons.model.playlistList
import com.example.tabbuttons.model.songModel

class AlertBoxAdapter(playlistList: MutableList<String>, writePlaylistName: EditText, SongModel: List<songModel>) : RecyclerView.Adapter<AlertBoxAdapter.AlertBoxViewHolder>() {

    var editTextName = writePlaylistName
    var listOfSongs = SongModel

    inner class AlertBoxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songNameInAlert = itemView.findViewById<TextView>(R.id.alertBox_playList_name)
        var edit = editTextName

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertBoxViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.alert_box_rv,parent, false)
        return AlertBoxViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertBoxViewHolder, position: Int) {
        holder.itemView.apply {
        holder.songNameInAlert.text =  playlistList[position]
        }

        holder.songNameInAlert.setOnClickListener {


            holder.edit.setText(playlistList[position])
        }

        Log.i("Name", playlistList[position])
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }

}