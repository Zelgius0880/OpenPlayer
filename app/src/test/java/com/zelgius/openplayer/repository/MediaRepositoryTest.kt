package com.zelgius.openplayer.repository

import com.thetransactioncompany.jsonrpc2.client.RawResponseInspector
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.model.Track
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail


internal class MediaRepositoryTest {

    private val repository by lazy {
        MediaRepository("http://127.0.0.1:3000", true).apply {
            session.rawResponseInspector =
                RawResponseInspector { response -> // print the HTTP status code
                    println("HTTP status: " + response.statusCode)
                }
        }
    }

    @Test
    fun getAlbumList() {
        runBlocking {
            with(repository.getAlbumList() ?: fail { "response is null" }) {
                Assertions.assertTrue(isNotEmpty())
            }
        }
    }

    @Test
    fun getAlbum() {
        runBlocking {
            val uri = "local:album:md5:eedc6e1effa3965cd04df71e67d515fb"
            with(repository.getTrackList(Album("", "", uri)) ?: fail { "response is null" }) {
                Assertions.assertTrue(isNotEmpty())
            }
        }
    }


    @Test
    fun getAlbumImage() {
        runBlocking {
            val uri = "local:album:md5:bea9052effbfd70f936524c236351ac6"
            with(repository.getAlbumImage(uri) ?: fail { "response is null" }) {
                Assertions.assertTrue(this.containsKey(uri))
                Assertions.assertTrue(this[uri]?.isNotEmpty() == true)
            }
        }
    }


    @Test
    fun getTrack() {
        runBlocking {
            with(
                repository.getTrack(
                    Track(
                        name = "",
                        type = "",
                        uri = "local:track:The%20Beatles%20-%20Let%20It%20Be.mp3",
                        Album("", "", "uri")
                    )
                )
            ) {
                Assertions.assertNotNull(this)
                Assertions.assertTrue(this?.readBytes()?.size ?: -1 > 0)
                this?.close()
            }
        }
    }

}