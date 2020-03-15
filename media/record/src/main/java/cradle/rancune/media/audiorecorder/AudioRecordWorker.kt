@file:Suppress("FunctionName")

package cradle.rancune.media.audiorecorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Process
import cradle.rancune.media.EncodedData
import java.lang.ref.WeakReference
import kotlin.math.ceil

/**
 * Created by Rancune@126.com 2020/3/3.
 */
class AudioRecordWorker(
    private val config: Config,
    private val listener: Listener
) :
    Runnable {

    companion object {
        const val TAG = "AudioWorker"

        const val MSG_START = 1
        const val MSG_STOP = 2
        const val MSG_FRAME = 3

        const val ERROR_AUDIO_MIN_BUFFER = 1
        const val ERROR_CREATE_AUDIORECORD = 2
        const val ERROR_CREATE_ENCODER = 3
        const val ERROR_START_AUDIORECORD = 4
        const val ERROR_START_ENCODER = 5
        const val ERROR_ENCODE_AUDIO = 6
        const val ERROR_STOP_AUDIORECORD = 7
        const val ERROR_STOP_ENCODER = 8

        const val STATE_AUDIO_START = 101
        const val STATE_AUDIO_STOP = 102
    }

    class Config {
        /**
         * 音频采样频率，单位：赫兹（Hz），常用采样频率：8000，12050，22050，44100等
         *
         * @see AudioRecord
         * 目前44100可以保证在所有的android上工作
         */
        var sampleRate = 44100

        /**
         * 声道
         * AudioFormat.CHANNEL_IN_MONO保证在所有的android上工作
         */
        var channel = AudioFormat.CHANNEL_IN_MONO

        /**
         * 每个声音采样点用16bit表示
         * AudioFormat.ENCODING_PCM_16BIT保证在所有的android上工作
         */
        var encodingFormat = AudioFormat.ENCODING_PCM_16BIT

        /**
         * 音频帧的采样点，与编码格式有关
         * 一般AAC的是1024采样点
         * 所以一帧的播放时间为1024 * 1000/44100= 22.32ms
         * https://blog.csdn.net/lu_embedded/article/details/50784355
         */
        var samplePerFrame = 1024
        var minBufferSize = samplePerFrame

        var mime = "audio/mp4a-latm"
        /**
         * 音频码率，单位：比特每秒（bit/s），常用码率：64k，128k，192k，256k，320k等。
         * 原如的码率 44100*16*1 = 705600 bit/s = 705600/8 byte/s = 705600/8/1000 kb/s ≈ 86.13k
         * 经过压缩算法压缩后，使用64k
         */
        var bitRate = 64000

        val sizeOfChannel: Int
            get() {
                if (channel == AudioFormat.CHANNEL_IN_MONO) {
                    return 1
                } else if (channel == AudioFormat.CHANNEL_IN_STEREO) {
                    return 2
                }

                return 1
            }

        val byteOfFormat: Int
            get() {
                if (encodingFormat == AudioFormat.ENCODING_PCM_16BIT) {
                    return 2
                } else if (encodingFormat == AudioFormat.ENCODING_PCM_8BIT) {
                    return 1
                }
                return 1
            }

    }

    interface Listener {

        fun onState(state: Int)

        fun onError(code: Int, e: Throwable? = null, extra: Any? = null)

        fun onFormatChanged(format: MediaFormat)

        fun onOutputAvailable(data: EncodedData)
    }

    private val lock = Object()
    private var handler: Handler? = null
    private var ready = false
    private var running = false
    private var stopped = false

    private var audioRecord: AudioRecord? = null
    private var audioEncoder: AudioEncoder? = null

    private var encoderFactory: AudioEncoder.Factory = AudioEncoder.MEDIA_CODEC

    fun start() {
        synchronized(lock) {
            if (running) {
                return
            }
            running = true
            Thread(
                this,
                TAG
            ).start()
            while (!ready) {
                try {
                    lock.wait()
                } catch (ignored: InterruptedException) {
                }
            }
        }
        handler?.sendEmptyMessage(MSG_START)
    }

    fun stop() {
        synchronized(lock) {
            if (!running) {
                return
            }
            if (stopped) {
                return
            }
            stopped = true
        }
        handler?.sendEmptyMessage(MSG_STOP)
    }

    fun setEncoderFactory(factory: AudioEncoder.Factory) {
        this.encoderFactory = factory
    }

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        Looper.prepare()
        synchronized(lock) {
            handler =
                AudioHandler(this)
            ready = true
            lock.notifyAll()
        }
        Looper.loop()
    }

    private fun _start() {
        val sampleRate: Int = config.sampleRate
        val channelConfig: Int = config.channel
        val format: Int = config.encodingFormat
        val samplePerFrame: Int = config.samplePerFrame

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, format)
        if (minBufferSize <= 0) {
            if (handleFailure(ERROR_AUDIO_MIN_BUFFER)) {
                return
            }
        }
        val bufferSize =
            ceil(minBufferSize.toFloat() / samplePerFrame.toFloat()).toInt() * samplePerFrame
        config.minBufferSize = bufferSize

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                format,
                bufferSize
            )
        } catch (e: Exception) {
            if (handleFailure(ERROR_CREATE_AUDIORECORD)) {
                return
            }
        }

        try {
            audioEncoder = encoderFactory.create(
                config,
                listener
            )
        } catch (e: Exception) {
            if (handleFailure(ERROR_CREATE_ENCODER)) {
                return
            }
        }

        try {
            audioRecord?.startRecording()
        } catch (e: Exception) {
            if (handleFailure(ERROR_START_AUDIORECORD)) {
                return
            }
        }

        try {
            audioEncoder?.start()
        } catch (e: Exception) {
            if (handleFailure(ERROR_START_ENCODER)) {
                return
            }
        }

        listener.onState(STATE_AUDIO_START)
        handler?.sendEmptyMessage(MSG_FRAME)
    }

    private fun _frame(endOfStream: Boolean = false) {
        try {
            audioEncoder?.encode(record = audioRecord!!, endOfStream = endOfStream)
        } catch (e: Exception) {
            if (handleFailure(ERROR_ENCODE_AUDIO)) {
                return
            }
        }
        synchronized(lock) {
            if (!running || stopped) {
                return
            }
        }
        handler?.sendEmptyMessage(MSG_FRAME)
    }

    private fun _stop() {
        try {
            audioEncoder?.stop()
            audioEncoder = null
        } catch (e: Exception) {
            listener.onError(ERROR_STOP_ENCODER, e, null)
        }
        try {
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            listener.onError(ERROR_STOP_AUDIORECORD, e, null)
        }
        listener.onState(STATE_AUDIO_STOP)
        ready = false
        running = false
        stopped = false
        handler?.looper?.quit()
        handler = null
    }

    private fun handleFailure(
        error: Int,
        throwable: Throwable? = null,
        extra: Any? = null
    ): Boolean {
        listener.onError(error, throwable, extra)
        return true
    }

    class AudioHandler(worker: AudioRecordWorker) : Handler() {
        private val ref = WeakReference<AudioRecordWorker>(worker)

        override fun handleMessage(msg: Message) {
            val workder = ref.get() ?: return
            when (msg.what) {
                MSG_START -> {
                    workder._start()
                }
                MSG_FRAME -> {
                    workder._frame(false)
                }
                MSG_STOP -> {
                    workder._frame(true)
                    workder._stop()
                }
            }
        }
    }
}