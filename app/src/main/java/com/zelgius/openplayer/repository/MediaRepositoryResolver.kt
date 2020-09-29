package com.zelgius.openplayer.repository

import com.zelgius.openplayer.model.Album

open class MediaRepositoryResolver(
    var outsideRepository: MediaRepository? = null,
    val insideRepository: MediaRepository = MediaRepository("https://192.168.1.63", true)
) {

    suspend fun getAlbumList(): List<Album>? {

        return resolveRepository {
            getAlbumList()
        }
    }

    private suspend fun <T> resolveRepository(block: suspend MediaRepository.() -> T?): T? {

        val (insideStatus, outsideStatus) = insideRepository.status to  outsideRepository?.status

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