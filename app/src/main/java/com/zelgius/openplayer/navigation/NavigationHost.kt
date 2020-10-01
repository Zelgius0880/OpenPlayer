package com.zelgius.openplayer.navigation

import android.os.Parcelable
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import com.zelgius.openplayer.MediaViewModel
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.ui.destination.Home
import com.zelgius.openplayer.ui.destination.TrackList
import kotlinx.android.parcel.Parcelize


sealed class Destination : Parcelable {

    @Parcelize
    object Home : Destination()

    @Parcelize
    class TrackList(val album: Album) : Destination()
}


abstract class Actions(private val navigator: Navigator<Destination>) {
    abstract val destination: Destination

    fun navigate() {
        navigator.navigate(destination = destination)
    }

    val pressOnBack: () -> Unit = {
        navigator.back()
    }
}


@Composable
fun NavigationHost(backDispatcher: OnBackPressedDispatcher) {
    val navigator: Navigator<Destination> = rememberSavedInstanceState(
        saver = Navigator.saver(backDispatcher)
    ) {
        Navigator(Destination.Home, backDispatcher)
    }
    val actions = remember(navigator) { /*Actions(navigator)*/ }

    Providers(BackDispatcherAmbient provides backDispatcher) {
        ProvideDisplayInsets {
            Crossfade(navigator.current) { destination ->
                when (destination) {
                    is Destination.Home -> {
                        Home(){
                            navigator.navigate(it)
                        }
                    }
                    is Destination.TrackList -> {
                        TrackList(destination.album)
                    }
                }
            }
        }
    }
}