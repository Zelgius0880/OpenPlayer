package com.zelgius.openplayer.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.ImageAssetConfig
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.res.vectorResource
import com.zelgius.openplayer.BuildConfig
import com.zelgius.openplayer.repository.UnsafeOkHttpClientBuilder
import kotlinx.coroutines.*
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.InputStream


@Composable
fun UrlImage(url: String?, default: Int, modifier: Modifier = Modifier) {
    val state = mutableStateOf<ImageAsset?>(null)

    if (url != null) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                with(
                    UnsafeOkHttpClientBuilder().build()
                        .newCall(
                            Request.Builder()
                                .url(url)
                                .header("X-Auth-Token", BuildConfig.KEY)
                                .build()
                        )
                        .execute()
                ) {
                    val `in` = body
                    if (`in` != null) {
                        val inputStream = `in`.byteStream()
                        val bufferedInputStream = BufferedInputStream(inputStream)
                        BitmapFactory.decodeStream(bufferedInputStream)?.let {
                            MainScope().launch {
                                state.value = RemoteImage(it)
                            }
                        }

                    }

                }

            }
        }
    }
    if (state.value == null)
        Image(asset = vectorResource(id = default), modifier = modifier)
    else Image(asset = state.value!!, modifier = modifier)
}

class RemoteImage(private val bitmap: Bitmap) : ImageAsset {
    override val width = bitmap.width
    override val height = bitmap.height
    override val config = ImageAssetConfig.Argb8888
    override val colorSpace = ColorSpaces.Srgb
    override val hasAlpha = bitmap.hasAlpha()
    override fun prepareToDraw() = bitmap.prepareToDraw()
    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int
    ) {
        var recycleTarget = false
        val targetBitmap =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                bitmap.config != Bitmap.Config.HARDWARE
            ) {
                bitmap
            } else {
                // Because we are creating a copy for the purposes of reading pixels out of it
                // be sure to recycle this temporary bitmap when we are finished with it.
                recycleTarget = true

                // Pixels of a hardware bitmap cannot be queried directly so make a copy
                // of it into a configuration that can be queried
                // Passing in false for the isMutable parameter as we only intend to read pixel
                // information from the bitmap
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            }

        targetBitmap.getPixels(
            buffer,
            bufferOffset,
            stride,
            startX,
            startY,
            width,
            height
        )
        // Recycle the target if we are done with it
        if (recycleTarget) {
            targetBitmap.recycle()
        }
    }
}