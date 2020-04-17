package cradle.rancune.once.view.player

import android.media.AudioFormat
import android.media.AudioManager
import android.os.Bundle
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.media.audioplayer.AudioTrackPlayer
import cradle.rancune.once.R
import cradle.rancune.once.view.base.BaseActivity
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.concurrent.thread

/**
 * Created by Rancune@126.com 2020/3/15.
 */
class PCMPlayerActivity : BaseActivity() {

    private var player: AudioTrackPlayer? = null

    @Volatile
    private var isPlaying = true

    private val config = AudioTrackPlayer.Config()

    init {
        config.sampleRate = 44100
        config.channel = AudioFormat.CHANNEL_OUT_MONO
        config.audioEncodingFormat = AudioFormat.ENCODING_PCM_16BIT
        config.streamStype = AudioManager.STREAM_MUSIC
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.once_activity_pcm_player)
        player = AudioTrackPlayer(config)
        player?.prepare()
        player?.start()
        isPlaying = true
        start()
    }


    private fun start() {
        thread {
            var input: InputStream? = null
            try {
                val dir = this.getExternalFilesDir("audio")
                val file = File(dir, "test.pcm")
                input = FileInputStream(file)
                val length = config.minBufferSize
                val array = ByteArray(length)
                while (isPlaying && input.available() > 0) {
                    val read = input.read(array, 0, length)
                    if (read > 0) {
                        player?.offer(array, 0, read)
                    }
                }
            } catch (e: Exception) {
                AndroidLog.e("PCMPlayerActivity", "", e)
            }
        }
    }

    override fun onDestroy() {
        isPlaying = false
        player?.release()
        super.onDestroy()
    }
}