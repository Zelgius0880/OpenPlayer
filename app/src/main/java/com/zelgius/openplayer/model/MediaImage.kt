package com.zelgius.openplayer.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaImage(
    val uri: String,
    val width: Int,
    val height: Int
): Parcelable