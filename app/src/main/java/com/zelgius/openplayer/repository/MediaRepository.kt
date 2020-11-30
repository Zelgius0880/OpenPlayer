package com.zelgius.openplayer.repository

import android.media.MediaDataSource
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session
import com.zelgius.openplayer.BuildConfig
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.model.MediaImage
import com.zelgius.openplayer.model.Track
import com.zelgius.openplayer.parseAsJsonArray
import com.zelgius.openplayer.repository.UnsafeOkHttpClientBuilder.Companion.trustAllCerts
import io.grpc.KnownLength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


class MediaRepository(private val url: String, debug: Boolean = false) {
    enum class Status {
        AVAILABLE, UNAVAILABLE, UNKNOWN
    }


    private val client by lazy {
        if (url.startsWith("https")) {
            UnsafeOkHttpClientBuilder().also {
                it.builder.apply {
                    connectTimeout(500L, TimeUnit.MILLISECONDS)
                    if (debug) {
                        val logger = HttpLoggingInterceptor()
                        logger.level = HttpLoggingInterceptor.Level.BODY
                        addInterceptor(logger)
                    }
                }
            }.build()
        } else {
            OkHttpClient.Builder().apply {
                connectTimeout(500L, TimeUnit.MILLISECONDS)
                if (debug) {
                    val logger = HttpLoggingInterceptor()
                    logger.level = HttpLoggingInterceptor.Level.BODY
                    addInterceptor(logger)
                }
            }.build()
        }
    }

    var status = Status.UNKNOWN

    private var requestID = 0

    private suspend fun isAvailable() =
        withContext(Dispatchers.IO) {
            try {
                service.checkAvailability().execute().isSuccessful
            } catch (e: IOException) {
                false
            }
        }


    suspend fun refreshStatus() = withContext(Dispatchers.IO) {
        status = if (isAvailable()) Status.AVAILABLE else Status.UNAVAILABLE
    }

    // The JSON-RPC 2.0 server URL
    //"http://127.0.0.1:6680/mopidy/rpc"
    private var rpcServerURL: URL = URL("$url/player/rpc")

    val session = JSONRPC2Session(rpcServerURL).apply {
        options.requestContentType = "application/json"
        options.trustAllCerts(true)
        connectionConfigurator = ConnectionConfigurator {
            it.addRequestProperty("X-Auth-Token", BuildConfig.KEY)
            (it as? HttpsURLConnection)?.let { c ->
                // Install the all-trusting trust manager
                // Install the all-trusting trust manager
                val sc = SSLContext.getInstance("SSL")
                sc.init(null, trustAllCerts, SecureRandom())
                c.sslSocketFactory = sc.socketFactory

                val allHostsValid = HostnameVerifier { _, _ -> true }

                c.hostnameVerifier = allHostsValid
            }
        }
    }

    private var retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .apply {
            client(client)
        }
        .build()

    private var service: TrackService = retrofit.create(TrackService::class.java)

    suspend fun getAlbumList() =
        call(
            method = "core.library.browse",
            params = mapOf("uri" to "local:directory?type=album")
        )?.parseAsJsonArray<Album>()


    suspend fun getAlbumListWithImages(): List<Album>? {
        val albums = call(
            method = "core.library.browse",
            params = mapOf("uri" to "local:directory?type=album")
        )?.parseAsJsonArray<Album>()
        if(albums != null) {
            val uris = albums.map { it.uri }
            val images = getAlbumImage(*uris.toTypedArray())
            albums.forEach {
                it.images = images[it.uri] ?: listOf()
            }
        }

        return albums
    }


    suspend fun getAlbumImage(vararg uris: String) =
        with(
            call(
                method = "core.library.get_images",
                params = mapOf("uris" to uris)
            )
        ){
            val type = object : TypeToken<Map<String, List<MediaImage>>>() {}.type
            val map: Map<String, List<MediaImage>> = Gson().fromJson(this, type)
            /*map.mapValues {
                val list = it.value.map { i -> i.copy(uri = "$url/${i.uri.replace("\\", "")}")}
                list
            }*/

            map
        }

    suspend fun getTrackList(album: Album) =
        call(
            method = "core.library.browse",
            params = mapOf("uri" to album.uri)
        )?.parseAsJsonArray<Album>()?.map {
            Track(it.name, it.type, it.uri, album).apply {
                serverIp = url
            }
        }

    suspend fun getImageStream(image: MediaImage) = withContext(Dispatchers.IO) {
        service.getImageStream(BuildConfig.KEY, image.uri
            .replace("\\", "")
            .replace("/", "")
            .replaceFirst("local", ""))
            .execute()
    }

    private suspend fun call(method: String, params: Map<String, Any>) =
        withContext(
            Dispatchers.IO
        ) {
            try {
                val request = JSONRPC2Request(method, params, requestID)
                val response = session.send(request)

                ++requestID

                if (response!!.indicatesSuccess()) {
                    println(response.result)

                    if (response.result is JSONObject)
                        (response.result as JSONObject).toJSONString()
                    else
                        (response.result as JSONArray).toJSONString()
                } else {
                    println(response.error.message)
                    error(response.error)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

    interface TrackService {
        @GET("/")
        fun checkAvailability(): Call<ResponseBody>

        @GET("/track/{name}")
        fun getTrack(
            @Header("X-Auth-Token") key: String,
            @Path("name") name: String
        ): Call<ResponseBody>


        @GET("/local/{uri}")
        fun getImageStream(
            @Header("X-Auth-Token") key: String,
            @Path("uri") uri: String
        ): Call<ResponseBody>
    }
}

fun String.decodeUri() = URLDecoder.decode(this, "UTF-8")

fun String.encodeUri() = URLEncoder.encode(this, "UTF-8")

class UnsafeOkHttpClientBuilder {
    companion object {
        val trustAllCerts: Array<TrustManager> = arrayOf(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )
    }
    val builder = OkHttpClient.Builder()

    fun build(): OkHttpClient {
        return try {
            // Create a trust manager that does not validate certificate chains


            // Install the all-trusting trust manager
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.connectionSpecs(
                listOf(
                    ConnectionSpec.MODERN_TLS,
                    ConnectionSpec.COMPATIBLE_TLS
                )
            )
            builder.hostnameVerifier { _, _ -> true }
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}