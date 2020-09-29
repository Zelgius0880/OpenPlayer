package com.zelgius.openplayer.repository

import com.nhaarman.mockitokotlin2.*
import com.zelgius.openplayer.model.Album
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

internal class MediaRepositoryResolverTest {
    private val listFirst = mock<List<Album>>()
    private val listSecond = mock<List<Album>>()

    private val outside: MediaRepository = mock {
        onBlocking { getAlbumList() } doReturn listSecond
    }
    private val inside: MediaRepository = mock {
        onBlocking { getAlbumList() } doReturn listFirst
    }

    private val resolver: MediaRepositoryResolver =
        MediaRepositoryResolver(insideRepository = inside, outsideRepository = outside)

    @BeforeEach
    fun beforeEach() {
    }

    @Test
    fun `when the two urls are available`() {
        outside.stub {
            on { status } doReturn MediaRepository.Status.AVAILABLE
        }

        inside.stub {
            on { status } doReturn MediaRepository.Status.AVAILABLE
        }

        runBlocking {
            assertNotEquals(resolver.getAlbumList(), listSecond)
            assertEquals(resolver.getAlbumList(), listFirst)
        }
    }

    @Test
    fun `when the first urls is available`() {
        inside.stub {
            on { status } doReturn MediaRepository.Status.AVAILABLE
        }

        outside.stub {
            on { status } doReturn MediaRepository.Status.UNAVAILABLE
        }

        runBlocking {
            assertNotEquals(resolver.getAlbumList(), listSecond)
            assertEquals(resolver.getAlbumList(), listFirst)
        }
    }

    @Test
    fun `when the second urls is available`() {
        inside.stub {
            on { status } doReturn MediaRepository.Status.UNAVAILABLE
        }

        outside.stub {
            on { status } doReturn MediaRepository.Status.AVAILABLE
        }

        runBlocking {
            assertNotEquals(resolver.getAlbumList(), listFirst)
            assertEquals(resolver.getAlbumList(), listSecond)
        }
    }

    @Test
    fun `when the two urls are unavailable`() {
        inside.stub {
            on { status } doReturn MediaRepository.Status.UNAVAILABLE
        }

        outside.stub {
            on { status } doReturn MediaRepository.Status.UNAVAILABLE
        }

        runBlocking {
            assertNull(resolver.getAlbumList())
        }

        inside.stub {
            on { status } doReturn MediaRepository.Status.UNAVAILABLE doReturn MediaRepository.Status.UNAVAILABLE
        }

        outside.stub {
            on { status } doReturn MediaRepository.Status.UNAVAILABLE doReturn MediaRepository.Status.UNAVAILABLE
        }

        runBlocking {
            assertNull(resolver.getAlbumList())
        }
    }

    @Test
    fun `when first url comes available`() {

        whenever(inside.status)
            .doAnswer(
                ArrayAnswer(
                    MediaRepository.Status.UNAVAILABLE,
                    MediaRepository.Status.UNKNOWN,
                    MediaRepository.Status.AVAILABLE
                )
            )

        whenever(outside.status)
            .doAnswer(
                ArrayAnswer(
                    MediaRepository.Status.UNAVAILABLE,
                    MediaRepository.Status.UNKNOWN,
                    MediaRepository.Status.UNAVAILABLE
                )
            )

        runBlocking {
            assertNull(resolver.getAlbumList())
            assertNotNull(resolver.getAlbumList())
            verify(inside).refreshStatus()
            verify(outside).refreshStatus()
            assertNotEquals(resolver.getAlbumList(), listSecond)
            assertEquals(resolver.getAlbumList(), listFirst)
        }
    }

    @Test
    fun `when second url comes available`() {

        whenever(inside.status)
            .doAnswer(
                ArrayAnswer(
                    MediaRepository.Status.UNAVAILABLE,
                    MediaRepository.Status.UNKNOWN,
                    MediaRepository.Status.UNAVAILABLE
                )
            )

        whenever(outside.status)
            .doAnswer(
                ArrayAnswer(
                    MediaRepository.Status.UNAVAILABLE,
                    MediaRepository.Status.UNKNOWN,
                    MediaRepository.Status.AVAILABLE
                )
            )

        runBlocking {
            assertNull(resolver.getAlbumList())
            assertNotNull(resolver.getAlbumList())
            verify(inside).refreshStatus()
            verify(outside).refreshStatus()
            assertNotEquals(resolver.getAlbumList(), listFirst)
            assertEquals(resolver.getAlbumList(), listSecond)
        }
    }

    class ArrayAnswer<T>(vararg args: T) : Answer<T> {
        private val values: List<T> = args.asList()

        var index = 0
        override fun answer(invocation: InvocationOnMock?): T {
            val res = values[index]
            ++index
            if (index >= values.size) index = values.size - 1

            return res
        }

    }

}