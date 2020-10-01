package com.zelgius.openplayer.model

import android.os.Parcelable
import com.beust.klaxon.Json
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Album (
    override val name: String,
    override val type: String,
    override val uri: String
) :Parcelable, Media{
    @Json(ignored = true)
    @IgnoredOnParcel
    override var images: List<MediaImage> = listOf()
}