package cradle.rancune.once.view.player

import android.media.AudioFormat
import android.media.AudioManager
import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.internal.extension.findString
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.internal.utils.AppUtils
import cradle.rancune.internal.utils.IOUtils
import cradle.rancune.internal.utils.T
import cradle.rancune.media.audioplayer.AudioTrackPlayer
import cradle.rancune.once.R
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
    private val file = File(AppUtils.application.getExternalFilesDir("audio"), "test.pcm")

    init {
        config.sampleRate = 44100
        config.channel = AudioFormat.CHANNEL_OUT_MONO
        config.audioEncodingFormat = AudioFormat.ENCODING_PCM_16BIT
        config.streamStype = AudioManager.STREAM_MUSIC
    }

    override fun initView() {
        setContentView(R.layout.once_activity_pcm_player)
    }

    override fun initData() {
        if (!file.exists() || file.isDirectory) {
            T.showShort(findString(R.string.once_file_not_exists, file.absolutePath))
            return
        }
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
            } finally {
                IOUtils.closeQuietly(input)
            }
        }
    }

    override fun onDestroy() {
        isPlaying = false
        player?.release()
        super.onDestroy()
    }
}