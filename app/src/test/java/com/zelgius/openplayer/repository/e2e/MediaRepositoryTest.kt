package com.zelgius.openplayer.repository.e2e

import com.thetransactioncompany.jsonrpc2.client.RawResponseInspector
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.model.Track
import com.zelgius.openplayer.repository.MediaRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail


internal class MediaRepositoryTest {

    private val repository by lazy {
        //MediaRepository("https://192.168.1.63", true).apply {
        MediaRepository("http://localhost:6680", true).apply {
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
    fun getAlbumImageWithImages() {
        runBlocking {
            val map = repository
                .getAlbumImage(
                    *repository
                        .getAlbumList()!!
                        .map { it.uri }
                        .toTypedArray()) ?: fail { "response is null" }

            with(repository.getAlbumListWithImages()!!) {
                forEach {
                    assertTrue(map.containsKey(it.uri))
                    assertEquals(map[it.uri]?.size, it.images.size)
                    it.images.forEach {i ->
                        assertTrue(i.uri.startsWith("http"))
                    }
                }
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
                        uri = "local:track:Music/The%20Legend%20of%20Zelda%2030th%20Anniversary%20Concert/1-03%20The%20Wind%20Waker%20Medley.mp3",
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