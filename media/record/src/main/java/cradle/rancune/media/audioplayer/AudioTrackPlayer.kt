@file:Suppress("FunctionName")

package cradle.rancune.media.audioplayer

import android.media.AudioRecord
import android.media.AudioTrack
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.media.OnErrorListener
import cradle.rancune.media.OnInfoListener


/**
 * Created by Rancune@126.com 2020/3/14.
 */
class AudioTrackPlayer(private val config: Config) {

    companion object {
        const val TAG = "AudioPlayWorker"

        const val ERROR_AUDIOTRACK_MIN_BUFFER = 1
        const val ERROR_AUDIOTRACK_CREATED = 2
        const val ERROR_AUDIOTRACK_RELEASE = 3

        const val STATE_PREPARED = 100
        const val STATE_STARTED = 101
        const val STATE_PAUSED = 102
        const val STATE_RELEASED = 103
    }

    class Config {
        var sampleRate = -1

        var channel = -1

        var audioEncodingFormat = -1

        var streamStype: Int = -1
        var minBufferSize = -1
    }

    private var audioTrack: AudioTrack? = null

    private var infoListener: OnInfoListener? = null
    private var errorListener: OnErrorListener? = null

    fun prepare(): Boolean {
        val sampleRate: Int = config.sampleRate
        val channelConfig: Int = config.channel
        val encodingFormat: Int = config.audioEncodingFormat
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, encodingFormat)
        if (minBufferSize <= 0) {
            handleFailure(ERROR_AUDIOTRACK_MIN_BUFFER, null, null)
            return true
        }
        config.minBufferSize = minBufferSize
        try {
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
//                audioTrack = AudioTrack(
//                    config.streamStype,
//                    sampleRate,
//                    channelConfig,
//                    encodingFormat,
//                    minBufferSize,
//                    AudioTrack.MODE_STREAM
//                )
//            } else {
//                val attributes = AudioAttributes.Builder()
//                    .setLegacyStreamType(config.streamStype)
//                    .build()
//                val format = AudioFormat.Builder()
//                    .setSampleRate(config.sampleRate)
//                    .setEncoding(config.audioEncodingFormat)
//                    .setChannelMask(config.channel)
//                    .build()
//                audioTrack = AudioTrack(
//                    attributes, format, minBufferSize, AudioTrack.MODE_STREAM,
//                    AudioManager.AUDIO_SESSION_ID_GENERATE
//                )
//            }
            audioTrack = AudioTrack(
                config.streamStype,
                sampleRate,
                channelConfig,
                encodingFormat,
                minBufferSize,
                AudioTrack.MODE_STREAM
            )
        } catch (e: Exception) {
            if (handleFailure(ERROR_AUDIOTRACK_CREATED, e)) {
                return false
            }
        }
        infoListener?.onInfo(STATE_PREPARED)
        return true
    }

    fun offer(data: ByteArray, offsetInBytes: Int, sizeInBytes: Int) {
        if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
            audioTrack?.play()
            val write = audioTrack?.write(data, offsetInBytes, sizeInBytes)
            when (write) {
                AudioRecord.ERROR_INVALID_OPERATION -> {
                    AndroidLog.d("Rancune", "error: ERROR_INVALID_OPERATION")
                }
                AudioRecord.ERROR_BAD_VALUE -> {
                    AndroidLog.d("Rancune", "error: ERROR_BAD_VALUE")
                }
                AudioRecord.ERROR_DEAD_OBJECT -> {
                    AndroidLog.d("Rancune", "error: ERROR_DEAD_OBJECT")
                }
                else -> {
                    AndroidLog.d("Rancune", "write:$write")
                }
            }
        }
    }

    fun start() {
        val state = audioTrack?.state
        if (state == AudioTrack.STATE_INITIALIZED) {
            audioTrack?.play()
            infoListener?.onInfo(STATE_STARTED)
        }
    }

    fun pause() {
        val state = audioTrack?.state
        if (state == AudioTrack.STATE_INITIALIZED) {
            audioTrack?.pause()
            infoListener?.onInfo(STATE_PAUSED)
        }
    }

    fun release() {
        try {
            if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack?.stop()
                audioTrack?.flush()
            }
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            errorListener?.onError(ERROR_AUDIOTRACK_RELEASE, null, e)
        }
        infoListener?.onInfo(STATE_RELEASED)
    }

    private fun handleFailure(
        error: Int,
        throwable: Throwable? = null,
        extra: Any? = null
    ): Boolean {
        errorListener?.onError(error, extra, throwable)
        return true
    }
}