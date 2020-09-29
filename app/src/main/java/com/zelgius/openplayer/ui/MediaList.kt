package com.zelgius.openplayer.ui

import androidx.compose.foundation.Box
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Card
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.IconButton
import androidx.compose.material.ProvideEmphasis
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.zelgius.openplayer.R
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.model.Media
import com.zelgius.openplayer.model.Track
import com.zelgius.openplayer.parseAsJsonArray
import com.zelgius.openplayer.ui.preview.SAMPLE_ALBUM_LIST
import java.net.URI
import java.util.*

@Composable
fun BigMedialList(list: List<Media>) {
    val rows = mutableListOf<Pair<Media, Media?>>()
    for (i in list.indices step 2) {
        rows.add(list[i] to list.getOrNull(i + 1))
    }

    LazyColumnFor(rows, modifier = Modifier.fillMaxWidth()) { item ->
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            Box(padding = 4.dp) {
                BigMediaItem(item = item.first)
            }

            Box(padding = 4.dp) {
                if (item.second != null)
                    BigMediaItem(item = item.second!!)
            }
        }
    }
}

@Composable
fun InlineMedialList(list: List<Media>) {
    LazyColumnFor(list, modifier = Modifier.fillMaxWidth()) { item ->
        Box(padding = 8.dp) {
            InlineMediaItem(item = item)
        }
    }
}

@Composable
fun BigMediaItem(item: Media) {
    Card(Modifier.width(150.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                asset = vectorResource(R.drawable.ic_music),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
                modifier = Modifier.height(height = 100.dp) + Modifier.fillMaxWidth()
            )
            Row() {
                Column(Modifier.weight(1f, true)) {
                    Text(
                        text = item.name,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item is Track) {

                        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                            Text(
                                text = item.album.name,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                IconButton(
                    onClick = {},
                    icon = { Icon(asset = Icons.Default.MoreVert) },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun InlineMediaItem(item: Media) {
    Card() {
        Row(modifier = Modifier.padding(8.dp)) {
            Image(
                asset = vectorResource(R.drawable.ic_music),
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(height = 50.dp)
            )

            Column(Modifier.weight(1f, true) + Modifier.padding(start = 8.dp, end = 8.dp)) {
                Text(
                    text = item.name,
                    overflow = TextOverflow.Ellipsis
                )

                if (item is Track) {

                    ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                        Text(
                            text = item.album.name,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }


            Box(gravity = Alignment.Center) {
                IconButton(
                    onClick = {},
                    icon = { Icon(asset = Icons.Default.MoreVert) },
                )
            }

        }
    }

}

@Preview
@Composable
fun BigAlbumItemPreview() {
    BigMediaItem(item = Album("Album 1", "album", uri = "dflshijgsdluhj"))
}

@Preview
@Composable
fun InlineAlbumItemPreview() {
    InlineMediaItem(item = Album("Album 1", "album", uri = "dflshijgsdluhj"))
}


@Preview
@Composable
fun BigTrackItemPreview() {
    BigMediaItem(item = Track("Track 1", "album", uri = "dflshijgsdluhj", Album("Album 1", "", "")))
}

@Preview
@Composable
fun InlineTrackItemPreview() {
    InlineMediaItem(
        item = Track(
            "Track 1",
            "album",
            uri = "dflshijgsdluhj",
            Album("Album 1", "", "")
        )
    )
}

@Preview
@Composable
fun BigMedialListPreview() {
    val medias = (1..100).map {
        Track(
            "Track $it", "track", uri = UUID.randomUUID().toString(),
            Album("Album $it", "album", "Album $it")
        )
    }

    BigMedialList(list = medias)
}

@Preview
@Composable
fun InlineMedialListPreview() {
    val medias = (1..100).map {
        Track(
            "Track $it", "track", uri = UUID.randomUUID().toString(),
            Album("Album $it", "album", "Album $it")
        )
    }

    InlineMedialList(list = medias)
}
