package com.rahul_music_pod.tabbuttons.dataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rahul_music_pod.tabbuttons.model.SongModel

@Dao
interface SongDao {

    @Insert
     fun addSong(song: SongModel)

    @Delete
    fun removeSong(song: SongModel)

    @Query("SELECT * FROM `database` WHERE playListName LIKE :favorite")
    fun readAllData(favorite: String): List<SongModel>

    @Query("SELECT  DISTINCT playListName  FROM `database`")
     fun readDistinctNames(): List<String>

    @Query("SELECT * FROM `database` WHERE playListName LIKE :name ")
    fun readAllSongsFromPlaylist(name: String): List<SongModel>

    @Query("SELECT * FROM `database` WHERE playListName LIKE :name  AND songId LIKE :idOfSong")
    fun checkingSongsInPlaylist(idOfSong: Int?, name: String?): Boolean

    @Query("DELETE FROM    `database` WHERE playListName LIKE :name")
    fun deleteAllSongs(name: String)
}