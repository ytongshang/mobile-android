package cradle.rancune.media.audiorecorder

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Created by Rancune@126.com 2020/3/4.
 */
interface AudioCallback {

    companion object {
        const val ERROR_AUDIO_MIN_BUFFER = 1
        const val ERROR_AUDIO_CREATE_RECORD = 2
        const val ERROR_AUDIO_CREATE_ENCODER = 3
        const val ERROR_AUDIO_START_RECORD = 4
        const val ERROR_AUDIO_START_ENCODER = 5
        const val ERROR_AUDIO_ENCODER_OFFER = 6
        const val ERROR_AUDIO_ENCODER_CONSUME = 7
        const val ERROR_AUDIO_STOP_RECORD = 8
        const val ERROR_AUDIO_STOP_ENCODER = 9

        const val STATE_AUDIO_START = 101
        const val STATE_AUDIO_STOP = 102
    }

    fun onState(state: Int)

    fun onError(code: Int, e: Throwable? = null, extra: Any? = null)

    fun onFormatChanged(format: MediaFormat)

    fun onOutputAvailable(info: MediaCodec.BufferInfo, buffer: ByteBuffer)
}