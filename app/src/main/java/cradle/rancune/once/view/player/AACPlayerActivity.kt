package cradle.rancune.once.view.player

import android.media.AudioFormat
import android.media.AudioManager
import android.media.MediaFormat
import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.media.EncodedData
import cradle.rancune.media.OnDataListener
import cradle.rancune.media.OnErrorListener
import cradle.rancune.media.OnInfoListener
import cradle.rancune.media.audioplayer.AudioTrackPlayer
import cradle.rancune.media.decoder.MediaDecoder
import cradle.rancune.media.mediasource.FileExtractor
import cradle.rancune.once.Constant
import cradle.rancune.once.R
import cradle.rancune.once.view.decode.MediaCodecVideoPlayActivity
import java.io.File
import kotlin.concurrent.thread

/**
 * Created by Rancune@126.com 2020/3/15.
 */
class AACPlayerActivity : BaseActivity() {

    private var audioSource: FileExtractor? = null
    private var audioDecoder: MediaDecoder? = null

    private var audioPlayer: AudioTrackPlayer? = null

    @Volatile
    private var isPlaying = true

    private val config = AudioTrackPlayer.Config()

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
        isPlaying = true
        start()
    }

    private fun start() {
        thread {
            val f = File(getExternalFilesDir(Constant.AUDIO_FILE), "test.aac")
            audioSource = FileExtractor(f.absolutePath)
            audioSource?.prepare()
            audioSource?.findTrack("audio")
            val format = audioSource?.mediaFormat ?: return@thread
            audioDecoder = MediaDecoder(format, audioSource!!, null)
            audioDecoder?.setOnInfoListener(object : OnInfoListener {
                override fun onInfo(what: Int, extra: Any?) {
                    if (what == MediaDecoder.INFO_OUTPUT_MEDIAFORMAT_CHANGED) {
                        if (audioPlayer == null) {
                            val config = AudioTrackPlayer.Config()
                            val outputFormat = audioDecoder!!.outputFormat!!
                            config.streamStype = AudioManager.STREAM_MUSIC
                            // 采样率
                            config.sampleRate = outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            // channel
                            val channelCount =
                                outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            config.channel =
                                if (channelCount > 1) {
                                    AudioFormat.CHANNEL_OUT_STEREO
                                } else {
                                    AudioFormat.CHANNEL_OUT_MONO
                                }
                            // encoding bits
                            val encoding = outputFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
                            val encodingFormat =
                                if (encoding == 2) AudioFormat.ENCODING_PCM_16BIT else AudioFormat.ENCODING_PCM_8BIT
                            config.audioEncodingFormat = encodingFormat
                            audioPlayer = AudioTrackPlayer(config)
                            audioPlayer?.prepare()
                            audioPlayer?.start()
                        }
                    }
                }
            })
            audioDecoder?.setOnErrorListener(object : OnErrorListener {
                override fun onError(what: Int, extra: Any?, throwable: Throwable?) {
                    AndroidLog.e(
                        MediaCodecVideoPlayActivity.TAG,
                        "audioDecoder error, code:$what, extra:$extra",
                        throwable
                    )
                }
            })
            audioDecoder?.setOnDataListener(object : OnDataListener {
                override fun onOutputAvailable(data: EncodedData) {
                    audioPlayer?.offer(data.byteArray!!, data.offset, data.size)
                }
            })

            audioDecoder?.prepare()
            audioDecoder?.start()

            while (isPlaying) {
                audioDecoder?.offerDecoder()
                audioDecoder?.drainDecoder()
            }
        }
    }

    override fun onDestroy() {
        isPlaying = false
        audioSource?.close()
        audioDecoder?.release()
        audioPlayer?.release()
        audioPlayer = null
        super.onDestroy()
    }
}