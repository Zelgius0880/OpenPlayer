package com.zelgius.openplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.repository.MediaRepositoryResolver

class MediaViewModel() : ViewModel(){
    private val resolver = MediaRepositoryResolver()

    fun getAlbumList() = liveData {
        emit(resolver.getAlbumList())
    }
    fun getTrackList(album: Album) = liveData {
        emit(resolver.getTrackList(album = album))
    }
}