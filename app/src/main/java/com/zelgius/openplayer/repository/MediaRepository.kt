package com.zelgius.openplayer.repository

import com.beust.klaxon.Converter
import com.beust.klaxon.Klaxon
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session
import com.zelgius.openplayer.BuildConfig
import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.model.Media
import com.zelgius.openplayer.model.MediaImage
import com.zelgius.openplayer.model.Track
import com.zelgius.openplayer.parseAsJsonArray
import com.zelgius.openplayer.parseAsJsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.minidev.json.JSONArray
import net.minidev.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.net.URL


class MediaRepository(url: String, debug: Boolean = false) {

    private var requestID = 0

    // The JSON-RPC 2.0 server URL
    //"http://127.0.0.1:6680/mopidy/rpc"
    var serverURL: URL = URL("$url/player/rpc")

    val session = JSONRPC2Session(serverURL).apply {
        options.requestContentType = "application/json"
        options.trustAllCerts(true)
        connectionConfigurator = ConnectionConfigurator {
            it.addRequestProperty("X-Auth-Token", BuildConfig.KEY)
        }
    }

    private var retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .apply {
            if (debug)
                client(
                    OkHttpClient.Builder().also {
                        val logger = HttpLoggingInterceptor()
                        logger.level = HttpLoggingInterceptor.Level.BODY
                        it.addInterceptor(logger)
                    }.build()

                )
        }
        .build()

    private var service: TrackService = retrofit.create(TrackService::class.java)

    suspend fun getAlbumList() =
        call(
            method = "core.library.browse",
            params = mapOf("uri" to "local:directory?type=album")
        ).parseAsJsonArray<List<Album>>()


    suspend fun getAlbumImage(vararg uris: String) =
        call(
            method = "core.library.get_images",
            params = mapOf("uris" to uris)
        ).parseAsJsonObject<Map<String, List<MediaImage>>>()


    suspend fun getTrackList(album: Album) =
        call(
            method = "core.library.browse",
            params = mapOf("uri" to album.uri)
        ).parseAsJsonArray<Album>()?.map {
            Track(it.name, it.type, it.uri, album)
        }


    suspend fun getTrack(track: Track) =
        withContext(Dispatchers.IO) {
            with(
                service.getTrack(key = BuildConfig.KEY, track.uri.replace("local:track:", "")).execute()
            ) {
                this.body()?.byteStream()
            }
        }


    private suspend fun call(method: String, params: Map<String, Any>) =
        withContext(
            Dispatchers.IO
        ) {
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
        }

    interface TrackService{
        @GET("/track/{name}")
        fun getTrack(
            @Header("X-Auth-Token") key: String,
            @Path("name") name: String
        ): Call<ResponseBody>
    }
}