package com.zelgius.openplayer.ui.destination

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.viewinterop.viewModel
import com.zelgius.openplayer.MediaViewModel
import com.zelgius.openplayer.navigation.Destination
import com.zelgius.openplayer.ui.BigMedialList

@Composable
fun Home(onDestinationChanged: (Destination) -> Unit) {
    val viewModel = viewModel<MediaViewModel>()
    val albums = viewModel.getAlbumList().observeAsState()

    albums.value?.let {
        BigMedialList(list = it) {a ->
            onDestinationChanged(Destination.TrackList(album = a))
        }
    }
}