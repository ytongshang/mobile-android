package cradle.rancune.media

import android.media.AudioRecord
import cradle.rancune.media.audioencoder.AudioMediaCodecEncoder
import cradle.rancune.media.audioencoder.AudioPCMEncoder

/**
 * Created by Rancune@126.com 2020/3/18.
 */
interface AudioEncoder {

    companion object {
        val MEDIA_CODEC = object : Factory {
            override fun create(config: AudioConfig): AudioEncoder {
                return AudioMediaCodecEncoder(config)
            }
        }

        val PCM = object : Factory {
            override fun create(config: AudioConfig): AudioEncoder {
                return AudioPCMEncoder(config)
            }
        }
    }

    interface Factory {
        fun create(
            config: AudioConfig
        ): AudioEncoder
    }

    fun setOnInfoListener(infoListener: OnInfoListener?)

    fun setOnDataListener(dataListener: OnDataListener?)

    fun start()

    fun stop()

    fun encode(record: AudioRecord, endOfStream: Boolean)

}