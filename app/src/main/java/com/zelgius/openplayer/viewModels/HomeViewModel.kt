package com.zelgius.openplayer

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.zelgius.openplayer.media.*
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.model.Media
import com.zelgius.openplayer.model.MediaImage
import com.zelgius.openplayer.model.Track
import com.zelgius.openplayer.repository.MediaRepositoryResolver

class HomeViewModel(
    context: Context
) : ViewModel() {
    private var mediaId: String = "" // TODO do something with that
    private val resolver = MediaRepositoryResolver()

    private val _items = mutableLiveDataOf<List<Media>>()
    val items: LiveData<List<Media>>
        get() = _items


    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: List<MediaBrowserCompat.MediaItem>
        ) {
            val itemsList = children.map { child ->
                val subtitle = child.description.subtitle ?: ""

                when {
                    child.mediaId?.startsWith("local:track") == true -> Track(
                        child.mediaId!!,
                        child.description.title.toString(),
                        subtitle.toString(),
                        Album("", "", "")
                    )

                    child.mediaId?.startsWith("local:album") == true -> Album(
                        uri = child.mediaId!!,
                        name = child.description.title.toString()
                    ).apply{
                        child.description.iconUri?.let {
                            images = listOf(MediaImage(it.toString()))
                        }
                    }

                    else -> error("Uri not managed: ${child.mediaId}")
                }
            }
            _items.postValue(itemsList)
        }
    }


    /**
     * When the session's [PlaybackStateCompat] changes, the [items] need to be updated
     * so the correct [Media.stateRes] is displayed on the active item.
     * (i.e.: play/pause button or blank)
     */
    private val playbackStateObserver: Observer<PlaybackStateCompat> =
        Observer<PlaybackStateCompat> {
            val playbackState = it ?: EMPTY_PLAYBACK_STATE
            val metadata: MediaMetadataCompat =
                playbackServiceConnection?.nowPlaying?.value ?: NOTHING_PLAYING
            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
                _items.postValue(updateState(playbackState, metadata))
            }
        }


    /**
     * When the session's [MediaMetadataCompat] changes, the [items] need to be updated
     * as it means the currently active item has changed. As a result, the new, and potentially
     * old item (if there was one), both need to have their [Media.stateRes]
     * changed. (i.e.: play/pause button or blank)
     */
    private val mediaMetadataObserver: Observer<MediaMetadataCompat?> =
        Observer<MediaMetadataCompat?> {
            val playbackState: PlaybackStateCompat =
                playbackServiceConnection?.playbackState?.value ?: EMPTY_PLAYBACK_STATE
            val metadata = it ?: NOTHING_PLAYING
            if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) != null) {
                _items.postValue(updateState(playbackState, metadata))
            }
        }


    private val isConnectedObserver: Observer<Boolean> =
        Observer<Boolean> {
            if (it && mediaId.isBlank()) {
                mediaId = playbackServiceConnection?.rootMediaId ?: ""
                playbackServiceConnection?.subscribe(mediaId, subscriptionCallback)
            } else if (!it)
                mediaId = ""
        }


    private val playbackServiceConnection: PlaybackServiceConnection? =
        PlaybackServiceConnection.getInstance(
            context.applicationContext,
            ComponentName(context.applicationContext, PlaybackService::class.java)
        ).also {
            it.playbackState.observeForever(playbackStateObserver)
            it.nowPlaying.observeForever(mediaMetadataObserver)
            it.isConnected.observeForever(isConnectedObserver)
        }


    override fun onCleared() {
        super.onCleared()

        // Remove the permanent observers from the MusicServiceConnection.
        playbackServiceConnection?.playbackState?.removeObserver(playbackStateObserver)
        playbackServiceConnection?.nowPlaying?.removeObserver(mediaMetadataObserver)
        playbackServiceConnection?.isConnected?.removeObserver(isConnectedObserver)

        // And then, finally, unsubscribe the media ID that was being watched.
        playbackServiceConnection?.unsubscribe(mediaId, subscriptionCallback)
    }

    fun getTrackList(album: Album): LiveData<List<Track>> {

    }

    private fun updateState(
        playbackState: PlaybackStateCompat,
        mediaMetadata: MediaMetadataCompat
    ): List<Media> {

        val newResId = when (playbackState.isPlaying) {
            true -> R.drawable.ic_twotone_pause_24
            else -> R.drawable.ic_twotone_play_arrow_24
        }

        return items.value?.map {
            val useResId = if (it.uri == mediaMetadata.id) newResId else 0
            when (it) {
                is Track -> it.copy(stateRes = useResId)
                is Album -> it.copy(stateRes = useResId)
            }
        } ?: emptyList()
    }

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            return HomeViewModel(
                context.applicationContext
            ) as T
        }
    }
}
