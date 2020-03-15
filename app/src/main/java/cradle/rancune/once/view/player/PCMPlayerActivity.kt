package cradle.rancune.once.view.player

import android.media.AudioFormat
import android.media.AudioManager
import android.media.MediaExtractor
import android.os.Bundle
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.media.audioplayer.AudioTrackPlayer
import cradle.rancune.once.R
import cradle.rancune.once.view.base.BaseActivity
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Created by Rancune@126.com 2020/3/15.
 */
class PCMPlayerActivity : BaseActivity() {

    private var player: AudioTrackPlayer? = null
    private var t: MediaExtractor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.once_activity_pcm_player)
        val config = AudioTrackPlayer.Config()
        config.sampleRate = 44100
        config.channel = AudioFormat.CHANNEL_OUT_MONO
        config.encodingFormat = AudioFormat.ENCODING_PCM_16BIT
        config.streamStype = AudioManager.STREAM_MUSIC
        player = AudioTrackPlayer(config)
        player?.start()
        start()
    }

    private fun start() {
        var input: InputStream? = null
        try {
            val dir = this.getExternalFilesDir("audio")
            val file = File(dir, "test.pcm")
            input = FileInputStream(file)
            val length = 4096
            val array = ByteArray(length)
            while (input.available() > 0) {
                val read = input.read(array, 0, length)
                if (read > 0) {
                    player!!.write(array, 0, read)
                }
            }
        } catch (e: Exception) {
            AndroidLog.e("PCMPlayerActivity", "", e)
        }
    }

    override fun onDestroy() {
        player?.release()
        super.onDestroy()
    }
}