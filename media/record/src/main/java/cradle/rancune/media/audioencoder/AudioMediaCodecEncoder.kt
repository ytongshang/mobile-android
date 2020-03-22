package cradle.rancune.media.audioencoder

import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.media.EncodedData
import cradle.rancune.media.AudioEncoder
import cradle.rancune.media.audiorecorder.AudioRecordWorker

/**
 * Created by Rancune@126.com 2018/7/24.
 */
class AudioMediaCodecEncoder(
    private val config: AudioRecordWorker.Config,
    private val listener: AudioRecordWorker.Listener
) : AudioEncoder {

    companion object {
        private const val TAG = "AudioMediaCodecEncoder"
        private const val SEC2MICROSEC = 1000000
    }

    private var encoder: MediaCodec? = null

    /**
     * 每秒钟采集的原始PCM数据大小
     */
    private var sampleBytePerSec: Int = 0
    private var presentationTimeUs: Long = 0
    private var presentationInterval: Long = 0
    private var unExpectedEndOfStream = false

    override fun start() {
        sampleBytePerSec = config.sampleRate * config.sizeOfChannel * config.byteOfFormat
        val mineType = config.mime
        val format =
            MediaFormat.createAudioFormat(mineType, config.sampleRate, config.sizeOfChannel)
        format.setInteger(MediaFormat.KEY_BIT_RATE, config.bitRate)
        encoder = MediaCodec.createEncoderByType(mineType)
        encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder!!.start()
    }

    override fun stop() {
        sampleBytePerSec = 0
        presentationTimeUs = 0
        presentationInterval = 0
        unExpectedEndOfStream = false
        encoder?.stop()
        encoder?.release()
        encoder = null
    }

    override fun encode(record: AudioRecord, endOfStream: Boolean) {
        offer(record, endOfStream)
        consume(endOfStream)
    }

    private fun offer(record: AudioRecord, endOfStream: Boolean) {
        // get an input buffer
        val buffIndex = encoder!!.dequeueInputBuffer(0)
        if (buffIndex >= 0) {
            val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                encoder!!.getInputBuffer(buffIndex)!!
            } else {
                @Suppress("DEPRECATION")
                encoder!!.inputBuffers[buffIndex]!!
            }
            buffer.clear()
            // read from audioRecord
            val readSize = record.read(buffer, buffer.remaining())
            if (readSize == AudioRecord.ERROR
                || readSize == AudioRecord.ERROR_BAD_VALUE
                || readSize == AudioRecord.ERROR_INVALID_OPERATION
                || readSize == AudioRecord.ERROR_DEAD_OBJECT
            ) {
                AndroidLog.w(TAG, "read from AudioRecord failed, error:$readSize")
            } else {
                val flag = if (endOfStream) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                encoder!!.queueInputBuffer(buffIndex, 0, readSize, presentationTimeUs, flag)
                // 因为音频是连续的，所以可以根据采集数据的大小大体计算实际经过的时间
                presentationInterval =
                    (readSize.toFloat() / sampleBytePerSec * SEC2MICROSEC).toLong()
                presentationTimeUs += presentationInterval
            }
        } else if (endOfStream) {
            unExpectedEndOfStream = true
        }
    }

    private fun consume(endOfStream: Boolean) {
        while (true) {
            val info = MediaCodec.BufferInfo()
            val buffIndex = encoder!!.dequeueOutputBuffer(info, 0)
            if (buffIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // !endOfStream， 表示外部是没有结束的，但是这里读的数据为0,生产者生产的数据不够
                // unExpectedEndOfStream 外部结束了
                if (!endOfStream || unExpectedEndOfStream) {
                    break
                }
            } else if (buffIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // 输出格式发生了变化
                // 一般是第一帧的输出，格式的回调
                val format = encoder!!.outputFormat
                listener.onFormatChanged(format)
            } else if (buffIndex < 0) {
                // 一般也不会发生
                AndroidLog.w(TAG, "Mediacodec dequeueOutputBuffer, buffIndex < 0")
            } else {
                val buffer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    encoder!!.getOutputBuffer(buffIndex)!!
                } else {
                    @Suppress("DEPRECATION")
                    encoder!!.outputBuffers[buffIndex]!!
                }
                // 消耗编码后的数据
                val data = EncodedData()
                data.offset = 0
                data.size = info.size
                data.byteArray = ByteArray(data.size)
                buffer.get(data.byteArray!!, data.offset, data.size)
                data.bufferInfo = info
                listener.onOutputAvailable(data)
                encoder!!.releaseOutputBuffer(buffIndex, false)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    if (!endOfStream) {
                        // 还在继续录音
                        AndroidLog.w(TAG, "AudioEncoder.poll : reached end of stream unexpectedly")
                    } else {
                        AndroidLog.d(TAG, "AudioEncoder.poll : end of stream reached")
                    }
                    break
                }
            }
        }
    }
}