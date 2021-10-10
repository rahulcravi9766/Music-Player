package com.example.tabbuttons.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tabbuttons.R
import com.example.tabbuttons.`interface`.CustomItemClickListener
import com.example.tabbuttons.dataBase.SongDao
import com.example.tabbuttons.dataBase.SongDatabase
import com.example.tabbuttons.fragments.FavFragmentDirections
import com.example.tabbuttons.fragments.SongsInPlaylistFragmentDirections
import com.example.tabbuttons.fragments.TabFragment
import com.example.tabbuttons.fragments.TabFragmentDirections
import com.example.tabbuttons.model.musicService
import com.example.tabbuttons.model.playlistList
import com.example.tabbuttons.model.songModel
import com.example.tabbuttons.model.toMinutes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.trendyol.bubblescrollbarlib.BubbleScrollBar
import kotlinx.android.synthetic.main.alert_box.view.*
import kotlinx.coroutines.*

class SongListAdapter(
    SongModel: MutableList<songModel>,
    val onClicked : (songModel,Int)->Unit,
    views: View,
    var navigationNumber:Int,
    fragmentContext: FragmentActivity
) :
    RecyclerView.Adapter<SongListAdapter.SongListViewHolder>() {


//    var navController =
//        Navigation.findNavController(views)
    //we are navigating from home which is inside tab fragment. so we use Navigation. and pass views
    var listOfSongs = SongModel
   // var navigationNumber = navNum
    var context = fragmentContext
    lateinit var  optionButton : ImageButton

    lateinit var alertAdapter: AlertBoxAdapter
    private lateinit var songDao: SongDao


    //itemView = layout of the recycleView
    inner class SongListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val songName = itemView.findViewById<TextView>(R.id.song_name)!!
        val songDuration = itemView.findViewById<TextView>(R.id.artist_name)!!
        val songPhoto = itemView.findViewById<ImageView>(R.id.song_image)!!
        var optionMenu = itemView.findViewById<ImageButton>(R.id.option_button)!!



        //   val alertRecyclerView =itemView.findViewById<RecyclerView>(R.id.alertRecyclerViewForPlaylist)

        private var mCustomItemClickListener: CustomItemClickListener? = null

        init {

            if (navigationNumber==2){
                optionMenu.visibility=View.GONE
            }
            itemView.setOnClickListener(this)
            //for making the view to just under or near them clicked item
            optionMenu.setOnClickListener(this)
            songDao = SongDatabase.getDatabase(context).songDao()
        }

        fun setCustomItemClickListener(customItemClickListener: CustomItemClickListener) {
            this.mCustomItemClickListener = customItemClickListener
        }

        override fun onClick(view: View?) {
            when (view!!.id) {
                R.id.option_button -> {
                    popupMenus(view, adapterPosition)
                }
                else -> this.mCustomItemClickListener!!.onCustomItemClick(view, adapterPosition)
            }
        }
    }

//    private fun deleteSong(view: View?,adapterPosition: Int){
//        listOfSongs[adapterPosition.rem(adapterPosition)]
//    }

    private fun removeSongFromPlaylist(adapterPosition: Int) {
        Log.i("Checking", " remove working")
        CoroutineScope(Dispatchers.IO).launch {
            songDao.removeSong(listOfSongs[adapterPosition])
//            musicService!!.insidePlaylistList= emptyList()
            listOfSongs.removeAt(adapterPosition)
            withContext(Dispatchers.Main) {
                this@SongListAdapter.notifyItemRemoved(adapterPosition)
            }
        }
    }


    private fun popupMenus(view: View?, adapterPosition: Int) {
        val popUpMenu = PopupMenu(context, view)
        popUpMenu.inflate(R.menu.show_menu)

        if (navigationNumber==1){
            val add=popUpMenu.menu.findItem(R.id.delete)
            add.isVisible=false
        }

        if (navigationNumber==3){
            val add=popUpMenu.menu.findItem(R.id.add_to_playlist)
            add.isVisible=false
        }



        popUpMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add_to_playlist -> {

                    alertDialogBox(adapterPosition)
                    true
                }
                R.id.delete -> {
                    val builder = MaterialAlertDialogBuilder(context)
                    builder.setTitle("Remove Song")
                        .setMessage("Do you want to remove this song from playlist?")
                        .setPositiveButton("Yes") { _, _ ->
                            removeSongFromPlaylist(adapterPosition)


                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()

                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                    true

                }

                else -> true
            }
        }
        popUpMenu.show()
    }

    private fun alertDialogBox(adapterPosition: Int) {

        val alertView = View.inflate(context, R.layout.alert_box, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(alertView)

        val dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        musicService!!.readPlaylistNamesFromDatabase(context)

        alertView.alertRecyclerViewForPlaylist.apply {
            alertAdapter = AlertBoxAdapter(playlistList, alertView.write_playlist_name, listOfSongs)
            adapter = alertAdapter
            Log.i("Name", "check playList")
            layoutManager = GridLayoutManager(context, 2)
        }

        //add button
        alertView.add_button.setOnClickListener {
            val name = alertView.write_playlist_name.text.toString()
            Log.i("fav", name)

            var isChecked: Boolean
            GlobalScope.launch(Dispatchers.IO) {
                isChecked =
                    songDao.checkingSongsInPlaylist(listOfSongs[adapterPosition].songId, name)
                withContext(Dispatchers.Main) {
                    if (isChecked) {
                        Toast.makeText(
                            context,
                            "Song already exist in this playlist",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    } else {
                        listOfSongs[adapterPosition].playListName = name
                        playlistList.add(name)
                        Log.i("fav", name)
                        val timeStampId = System.currentTimeMillis()
                        listOfSongs[adapterPosition].timeStamp =
                            timeStampId.toString() + listOfSongs[adapterPosition].songId
                        if(listOfSongs[adapterPosition].playListName.contains("favorite")){
                            listOfSongs.remove(listOfSongs[adapterPosition])
                        }
                        addToPlaylist(adapterPosition)
                        Toast.makeText(
                            context,
                            "Song added to playlist $name",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.i("Playlist name", listOfSongs[adapterPosition].playListName)
                        dialog.dismiss()
                    }
                }
            }
        }

        //cancel button
        alertView.cancel_button.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun addToPlaylist(adapterPosition: Int) {
        Log.i("Playlist added", "working")
        System.currentTimeMillis()
        CoroutineScope(Dispatchers.IO).launch {
//            if(playlistList.contains("favorite")){
//                playlistList.remove("favorite")
//            }
            songDao.addSong(listOfSongs[adapterPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListViewHolder {
        //connecting the item_structure xml here
        Log.i("ListAdapter", "1")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.music_row, parent, false)

        return SongListViewHolder(view)
    }

    //bind data to the items
    //Take the data  and set it to the corresponding view
    override fun onBindViewHolder(holder: SongListViewHolder, selectedSongPosition: Int) {

            holder.songName.text = listOfSongs[selectedSongPosition].songName
            holder.songDuration.text =
                toMinutes(listOfSongs[selectedSongPosition].songDuration.toLong())
            Glide.with(context).load(listOfSongs[selectedSongPosition].songPhoto).apply(
                RequestOptions().placeholder(R.drawable.songimg).centerCrop()
            ).into(holder.songPhoto)

            holder.setCustomItemClickListener(object : CustomItemClickListener {

                override fun onCustomItemClick(view: View, selectedSongPosition: Int) {

                    Log.i("testPlayerCount",selectedSongPosition.toString())
                    onClicked(listOfSongs[selectedSongPosition],selectedSongPosition)

                    //navigating from home fragment which is inside tab fragment. So home fragment is not shown here
                    when (navigationNumber) {
                        1 -> {
                            //all songs
                            musicService!!.alteringListOfSongs(1)

                        }
                        2 -> {
                            //fav songs
                            musicService!!.alteringListOfSongs(2)

                        }
                        3 -> {
                            //songs in playlist
                            musicService!!.alteringListOfSongs(3)
                        }
                    }
                }
            })
    }

    override fun getItemCount(): Int {
        Log.i("SizeOfList", listOfSongs.toString() + "getItemCount")
        return listOfSongs.size
    }
}
