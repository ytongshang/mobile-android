package cradle.rancune.media.audioplayer

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Created by Rancune@126.com 2020/3/4.
 */
interface AudioPlayCallback {

    companion object {
        const val ERROR_AUDIOTRACK_MIN_BUFFER = 1
        const val ERROR_AUDIOTRACK_CREATED = 2
        const val ERROR_AUDIOTRACK_RELEASE = 3

        const val STATE_AUDIO_START = 101
        const val STATE_AUDIO_STOP = 102
    }

    fun onState(state: Int)

    fun onError(code: Int, e: Throwable? = null, extra: Any? = null)

    fun onFormatChanged(format: MediaFormat)

    fun onOutputAvailable(info: MediaCodec.BufferInfo, buffer: ByteBuffer)
}