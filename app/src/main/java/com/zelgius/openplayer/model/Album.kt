package com.zelgius.openplayer.model

import com.beust.klaxon.Json

data class Album (
    override val name: String,
    override val type: String,
    override val uri: String
) :Media{
    @Json(ignored = true)
    override var image: List<MediaImage> = listOf()
}