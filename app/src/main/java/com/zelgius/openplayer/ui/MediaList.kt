package com.zelgius.openplayer.ui

import androidx.compose.foundation.*
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
import java.util.*

@Composable
fun <T : Media> BigMedialList(list: List<T>, mediaClicked: (media: T) -> Unit = {}) {
    val rows = mutableListOf<Pair<T, T?>>()
    for (i in list.indices step 2) {
        rows.add(list[i] to list.getOrNull(i + 1))
    }

    LazyColumnFor(rows, modifier = Modifier.fillMaxWidth()) { item ->
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            Box(padding = 4.dp) {
                BigMediaItem(item = item.first) {
                    mediaClicked(it)
                }
            }

            Box(padding = 4.dp) {
                if (item.second != null)
                    BigMediaItem(item = item.second!!) { mediaClicked(it) }
            }
        }
    }
}

@Composable
fun <T : Media> InlineMedialList(list: List<T>, mediaClicked: (media: T) -> Unit = {}) {
    LazyColumnFor(list, modifier = Modifier.fillMaxWidth()) { item ->
        Box(padding = 8.dp) {
            InlineMediaItem(item = item) { mediaClicked(it) }
        }
    }
}

@Composable
fun <T : Media> BigMediaItem(item: T, onItemClick: (T) -> Unit = {}) {
    Card(
        Modifier.width(150.dp)
            .clickable(onClick = {
                onItemClick(item)
            })
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
                .clickable(onClick = {
                    onItemClick(item)
                })
        ) {
            Image(
                asset = vectorResource(R.drawable.ic_music),
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center,
                modifier = Modifier.height(height = 100.dp) then Modifier.fillMaxWidth()
            )
            Row {
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
fun <T : Media> BigInlineMediaItem(item: T, onClick: (T) -> Unit = {}) {
    Card(modifier = Modifier.clickable(onClick = { onClick(item) })) {
        Row(modifier = Modifier.padding(8.dp)) {
            Image(
                asset = vectorResource(R.drawable.ic_music),
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(height = 50.dp)
            )

            Column(Modifier.weight(1f, true) then Modifier.padding(start = 8.dp, end = 8.dp)) {
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

@Composable
fun <T : Media> InlineMediaItem(item: T, onClick: (T) -> Unit = {}) {
    Card(modifier = Modifier.clickable(onClick = { onClick(item) })) {
        Row(modifier = Modifier.padding(8.dp)) {
            Image(
                asset = vectorResource(R.drawable.ic_music),
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(height = 50.dp)
            )

            Column(Modifier.weight(1f, true) then Modifier.padding(start = 8.dp, end = 8.dp)) {
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

    BigMedialList(list = medias) {}
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

    InlineMedialList(list = medias) {}
}
