import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import android.util.Log
import com.zelgius.openplayer.media.MediaPlaybackHandler
import com.zelgius.openplayer.media.ServiceStub
import java.io.IOException




class PlaybackService : Service() {

    companion object {
        private const val FOCUS_CHANGE = 10
        private const val FADE_DOWN = 11
        private const val FADE_UP = 12
        private const val SERVER_DIED = 13
    }

    private val mNinder: IBinder = ServiceStub(this)
    private var mediaPlayback: MediaPlayback? = null
    private var audioManager: AudioManager? = null
    private var playerHandler: MediaPlaybackHandler? = null
    private val audioFocusListener: OnAudioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange ->
            playerHandler!!.obtainMessage(
                FOCUS_CHANGE,
                focusChange,
                0
            ).sendToTarget()
        }
    private var pausedByTransientLossOfFocus = false
    var isSupposedToBePlaying = false
    override fun onCreate() {
        super.onCreate()

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work will not disrupt the UI.
        val thread = HandlerThread(
            "MediaPlaybackHandler",
            Process.THREAD_PRIORITY_BACKGROUND
        )
        thread.start()

        // Initialize the handlers
        playerHandler = MediaPlaybackHandler(this, thread.looper)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        Log.i("PlaybackService", "MediaPlayback class instantiated")
        mediaPlayback = MediaPlayback(playerHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w("PlaybackService", "Destroying service")
        mediaPlayback!!.release()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mNinder
    }

    /**
     * Provides an interface for dealing with playback of audio files
     */
    private inner class MediaPlayback(var handler: Handler) : OnCompletionListener,
        MediaPlayer.OnErrorListener {
        private var player: MediaPlayer? = MediaPlayer()
        private var volume = 0f
            set(volume) {
                player!!.setVolume(volume, volume)
                field = volume
            }

        var isPlayerInitialised = false
        fun start() {
            player!!.start()
        }

        fun stop() {
            player!!.reset()
            isSupposedToBePlaying = false
        }

        /**
         * You CANNOT use this player after calling release
         */
        fun release() {
            stop()
            player!!.release()
        }

        fun pause() {
            player!!.pause()
        }

        val duration: Long
            get() = if (player != null && isPlayerInitialised) {
                player!!.duration.toLong()
            } else -1
        val position: Long
            get() {
                return if (player != null && isPlayerInitialised) {
                    player!!.currentPosition.toLong()
                } else 0
            }

        fun seek(whereTo: Long) {
            player!!.seekTo(whereTo.toInt())
        }


        fun setDataSource(path: String) {
            isPlayerInitialised = setDataSource(player, path)
        }

        private fun setDataSource(mediaPlayer: MediaPlayer?, path: String): Boolean {
            try {
                mediaPlayer?.apply {
                    reset()
                    setOnPreparedListener(null)

                    assets.openFd(path).let {
                        setDataSource(it.fileDescriptor, it.startOffset, it.length)
                        it.close()
                    }
                    setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    prepare()
                    setOnCompletionListener(this@MediaPlayback)
                    setOnErrorListener(this@MediaPlayback)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
                return false
            } catch (ex: SecurityException) {
                ex.printStackTrace()
                return false
            }
            return true
        }

        override fun onCompletion(mediaPlayer: MediaPlayer) {
            player!!.release()
            player = null
        }

        override fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int): Boolean {
            Log.e("PlaybackService", "Error: $what")
            when (what) {
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                    isPlayerInitialised = false
                    player!!.release()
                    player = MediaPlayer()
                    handler.sendMessageDelayed(handler.obtainMessage(SERVER_DIED), 2000)
                    return true
                }
            }
            return false
        }
    }

    fun stop() {

        //Todo: Fade down nicely
        synchronized(this) {
            if (mediaPlayback != null && mediaPlayback!!.isPlayerInitialised) {
                mediaPlayback!!.stop()
            }
        }
    }

    fun play() {
        synchronized(this) {
            audioManager!!.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            if (mediaPlayback!!.isPlayerInitialised) {
                mediaPlayback!!.start()
                playerHandler!!.removeMessages(FADE_DOWN)
                playerHandler!!.sendEmptyMessage(FADE_UP)
                isSupposedToBePlaying = true
            }
        }
    }

    fun pause() {

        //Todo: Fade down nicely
        synchronized(this) {
            playerHandler!!.removeMessages(FADE_UP)
            if (isSupposedToBePlaying) {
                mediaPlayback!!.pause()
                isSupposedToBePlaying = false
                pausedByTransientLossOfFocus = false
            }
        }
    }

    fun openFile(path: String?): Boolean {
        synchronized(this) {
            if (path == null) {
                return false
            }
            mediaPlayback!!.setDataSource(path)
            if (mediaPlayback!!.isPlayerInitialised) {
                return true
            }
            stop()
            return false
        }
    }

    val duration: Long
        get() {
            synchronized(this) {
                if (mediaPlayback != null && mediaPlayback!!.isPlayerInitialised) {
                    return mediaPlayback!!.duration
                }
            }
            return -1
        }
    val position: Long
        get() {
            synchronized(this) {
                if (mediaPlayback != null && mediaPlayback!!.isPlayerInitialised) {
                    return mediaPlayback!!.position
                }
            }
            return 0
        }

    fun seek(position: Long) {
        var pos = position
        synchronized(this) {
            if (mediaPlayback != null && mediaPlayback!!.isPlayerInitialised) {
                if (pos < 0) {
                    pos = 0
                } else if (pos > mediaPlayback!!.duration) {
                    pos = mediaPlayback!!.duration
                }
                mediaPlayback!!.seek(pos)
            }
        }
    }


}
//MainActivity:

//An Activity to demonstrate binding to the service.
/*
class MainActivity : Activity(), ServiceConnection {
    private var mToken: ServiceToken? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Let the device know that we're playing music, and volume controls should affect the music stream
        volumeControlStream = AudioManager.STREAM_MUSIC

        //Bind to our Music Service. This is where the magic happens!
        mToken = ServiceUtils.bindToService(this, this)
        val loadFileButton: Button = findViewById<View>(R.id.load_button) as Button
        loadFileButton.setOnClickListener(object : OnClickListener() {
            fun onClick(v: View?) {
                if (isPlaying) {
                    stop()
                }
                openFile("my_awesome_file.mp3")
            }
        })
        val playPauseButton: Button = findViewById<View>(R.id.play_pause_button) as Button
        playPauseButton.setOnClickListener(object : OnClickListener() {
            fun onClick(v: View?) {

                //Todo: Make sure we've loaded a file before attempting to play or pause!
                if (!isPlaying) {
                    play()
                } else {
                    pause()
                }
            }
        })
    }

    public override fun onDestroy() {

        //The activity is being destroyed. We should unbind from our service
        if (mToken != null) {
            ServiceUtils.unbindFromService(mToken)
            mToken = null
        }
        super.onDestroy()
    }

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        //Hooray, we've connected to our service.
        //If we've arrived here, we should be able to make any calls via our PlaybackUtils class, and expect the service to
        //respond accordingly
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        //The service is disconnected.. We don't really care about this.. do we?
    }
}*/
