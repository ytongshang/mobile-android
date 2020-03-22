package cradle.rancune.media

import android.media.AudioRecord
import cradle.rancune.media.audioencoder.AudioMediaCodecEncoder
import cradle.rancune.media.audioencoder.AudioPCMEncoder
import cradle.rancune.media.audiorecorder.AudioRecordWorker

/**
 * Created by Rancune@126.com 2020/3/18.
 */
interface AudioEncoder {

    companion object {
        val MEDIA_CODEC = object : Factory {
            override fun create(
                config: AudioRecordWorker.Config,
                listener: AudioRecordWorker.Listener
            ): AudioEncoder {
                return AudioMediaCodecEncoder(
                    config,
                    listener
                )
            }
        }

        val PCM = object : Factory {
            override fun create(
                config: AudioRecordWorker.Config,
                listener: AudioRecordWorker.Listener
            ): AudioEncoder {
                return AudioPCMEncoder(
                    config,
                    listener
                )
            }
        }
    }

    interface Factory {
        fun create(
            config: AudioRecordWorker.Config,
            listener: AudioRecordWorker.Listener
        ): AudioEncoder
    }

    fun start()

    fun stop()

    fun encode(record: AudioRecord, endOfStream: Boolean)

}