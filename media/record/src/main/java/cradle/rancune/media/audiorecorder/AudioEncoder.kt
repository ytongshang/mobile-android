package cradle.rancune.media.audiorecorder

import android.media.AudioRecord
import cradle.rancune.media.audiorecorder.encoder.AudioMediaCodecEncoder
import cradle.rancune.media.audiorecorder.encoder.AudioPCMEncoder

/**
 * Created by Rancune@126.com 2020/3/15.
 */
abstract class AudioEncoder(
    val config: AudioRecordWorker.Config,
    val listener: AudioRecordWorker.Listener
) {

    companion object {
        val MEDIA_CODEC = object : Factory {
            override fun create(
                config: AudioRecordWorker.Config,
                listener: AudioRecordWorker.Listener
            ): AudioEncoder {
                return AudioMediaCodecEncoder(config, listener)
            }
        }

        val PCM = object : Factory {
            override fun create(
                config: AudioRecordWorker.Config,
                listener: AudioRecordWorker.Listener
            ): AudioEncoder {
                return AudioPCMEncoder(config, listener)
            }
        }
    }

    interface Factory {
        fun create(
            config: AudioRecordWorker.Config,
            listener: AudioRecordWorker.Listener
        ): AudioEncoder
    }

    open fun start() {

    }

    open fun stop() {

    }

    abstract fun encode(record: AudioRecord, endOfStream: Boolean)
}
