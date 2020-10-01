package com.zelgius.openplayer.model

import com.beust.klaxon.Json

data class Track (
    override val name: String,
    override val type: String,
    override val uri: String,
    val album: Album
) : Media{
    @Json(ignored = true)
    override var images: List<MediaImage> = album.images
}