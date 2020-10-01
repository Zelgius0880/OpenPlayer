package com.zelgius.openplayer.model

import android.os.Parcelable
import com.beust.klaxon.Json
import kotlinx.android.parcel.Parcelize

interface Media {
    val name: String
    val type: String
    val uri: String
    var images: List<MediaImage>
}