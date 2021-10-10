package com.example.tabbuttons.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tabbuttons.model.songModel

@Database(entities = [songModel::class],version = 1,exportSchema = false)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    companion object{

        @Volatile
        private var INSTANCE:SongDatabase?=null

        fun getDatabase(context: Context):SongDatabase{
            val instance= INSTANCE
            if (instance==null){
                synchronized(this){
                    val instance= Room.databaseBuilder(
                        context.applicationContext,
                        SongDatabase::class.java,
                        "song_database"
                    ).build()
                    INSTANCE= instance
                    return instance
                }
            }
            return instance
        }
    }
}