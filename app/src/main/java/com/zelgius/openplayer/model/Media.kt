package com.zelgius.openplayer.model

import com.beust.klaxon.Json

interface Media {
    val name: String
    val type: String
    val uri: String
    var image: List<MediaImage>
}