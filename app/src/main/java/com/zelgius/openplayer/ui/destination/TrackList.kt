package com.zelgius.openplayer.ui.destination

import androidx.compose.foundation.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.viewinterop.viewModel
import com.zelgius.openplayer.MediaViewModel
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.ui.InlineMedialList

@Composable
fun TrackList(album: Album) {
    val viewModel = viewModel<MediaViewModel>()
    val tracks = viewModel.getTrackList(album).observeAsState()
    tracks.value?.let {
        InlineMedialList(list = it)
    }
}