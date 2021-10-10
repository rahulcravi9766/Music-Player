package com.example.tabbuttons.dataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.tabbuttons.model.songModel
import java.sql.Timestamp

@Dao
interface SongDao {

    @Insert
     fun addSong(song: songModel)

    @Delete
    fun removeSong(song: songModel)

    @Query("SELECT * FROM `database` WHERE playListName LIKE :favorite")
    fun readAllData(favorite: String): List<songModel>

    @Query("SELECT  DISTINCT playListName  FROM `database`")
     fun readDistinctNames(): List<String>

    @Query("SELECT * FROM `database` WHERE playListName LIKE :name ")
    fun readAllSongsFromPlaylist(name: String): List<songModel>

    @Query("SELECT * FROM `database` WHERE playListName LIKE :name  AND songId LIKE :idOfSong")
    fun checkingSongsInPlaylist(idOfSong: Int?, name: String?): Boolean

    @Query("DELETE FROM    `database` WHERE playListName LIKE :name")
    fun deleteAllSongs(name: String)
}