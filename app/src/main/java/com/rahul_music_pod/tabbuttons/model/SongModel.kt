package com.rahul_music_pod.tabbuttons.model

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rahul_music_pod.tabbuttons.service.PlayMusicService
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
@Entity(tableName = "database")
data class SongModel(
    @PrimaryKey var timeStamp : String,
    val songId: Int,
    var playListName : String,
    val songName: String,
    val songDuration: Long,
    val songPath: String,
    val songPhoto: String
) : Parcelable


 @SuppressLint("StaticFieldLeak")
 var musicService: PlayMusicService? = null
 var playlistList= mutableListOf<String>()

fun toMinutes(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%2d:%02d", minutes, seconds)
 }

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}


