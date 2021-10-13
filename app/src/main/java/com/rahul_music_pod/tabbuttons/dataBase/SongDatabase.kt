package com.rahul_music_pod.tabbuttons.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rahul_music_pod.tabbuttons.model.SongModel

@Database(entities = [SongModel::class],version = 1,exportSchema = false)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    companion object{

        @Volatile
        private var INSTANCE:SongDatabase?=null

        fun getDatabase(context: Context): SongDatabase {
            return INSTANCE
                ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        SongDatabase::class.java,
                        "song_database"
                    ).build()
                    INSTANCE = instance
                    return instance
                }
        }
    }
}