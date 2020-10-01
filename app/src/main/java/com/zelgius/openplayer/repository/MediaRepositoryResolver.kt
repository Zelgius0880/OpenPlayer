package com.zelgius.openplayer.repository

import androidx.lifecycle.LiveData
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.model.Track
import com.zelgius.openplayer.mutableLiveDataOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

open class MediaRepositoryResolver(
    var outsideRepository: MediaRepository? = null,
    val insideRepository: MediaRepository = MediaRepository("https://192.168.1.63", true)
) {
    private val _outsideCreated = mutableLiveDataOf(false)
    val outsideCreated: LiveData<Boolean>
        get() = _outsideCreated

    private val firebaseRepository = FirebaseRepository(true)

    private suspend fun createOutsideRepository(){
            firebaseRepository.getSnapshot("open-player", "ips").getString("ip")?.let {
                outsideRepository = MediaRepository("https://${it.trim()}")
            }
    }

    suspend fun getAlbumList(): List<Album>? {

        return resolveRepository {
            getAlbumList()
        }
    }

    suspend fun getTrackList(album: Album): List<Track>? {
        return resolveRepository {
            getTrackList(album = album)
        }
    }

    private suspend fun <T> resolveRepository(block: suspend MediaRepository.() -> T?): T? {
        if(outsideRepository == null) createOutsideRepository()

        val (insideStatus, outsideStatus) = insideRepository.status to outsideRepository?.status

        if (insideStatus == MediaRepository.Status.UNAVAILABLE && outsideStatus == MediaRepository.Status.UNAVAILABLE) {
            outsideRepository?.status = MediaRepository.Status.UNKNOWN
            insideRepository.status = MediaRepository.Status.UNKNOWN
            return null
        }

        if (insideStatus == MediaRepository.Status.UNKNOWN && outsideStatus == MediaRepository.Status.UNKNOWN) {
            outsideRepository?.refreshStatus()
            insideRepository.refreshStatus()
            return resolveRepository(block)
        }

        val repository = if (insideRepository.status == MediaRepository.Status.AVAILABLE)
            insideRepository
        else outsideRepository

        return repository?.let { it.block() }
    }

}