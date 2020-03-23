@file:Suppress("FunctionName")

package cradle.rancune.media.audiorecorder

import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Process
import cradle.rancune.media.*
import java.lang.ref.WeakReference
import kotlin.math.ceil

/**
 * Created by Rancune@126.com 2020/3/3.
 */
class AudioRecordWorker(private val config: AudioConfig) : Runnable {

    companion object {
        const val TAG = "AudioWorker"

        private const val MSG_START = 1
        private const val MSG_STOP = 2
        private const val MSG_FRAME = 3

        const val ERROR_AUDIO_MIN_BUFFER = 1
        const val ERROR_CREATE_AUDIORECORD = 2
        const val ERROR_CREATE_ENCODER = 3
        const val ERROR_START_AUDIORECORD = 4
        const val ERROR_START_ENCODER = 5
        const val ERROR_ENCODE_AUDIO = 6
        const val ERROR_STOP_AUDIORECORD = 7
        const val ERROR_STOP_ENCODER = 8

        const val INFO_AUDIO_START = 101
        const val INFO_AUDIO_STOP = 102
        const val INFO_OUTPUT_MEDIAFORMAT_CHANGED = 103
    }

    private val lock = Object()
    private var handler: Handler? = null
    private var ready = false
    private var running = false
    private var stopped = false

    private var audioRecord: AudioRecord? = null
    private var audioEncoder: AudioEncoder? = null
    private var encoderFactory: AudioEncoder.Factory = AudioEncoder.MEDIA_CODEC
    private var infoListener: OnInfoListener? = null
    private var errorListener: OnErrorListener? = null
    private var dataListener: OnDataListener? = null

    fun setOnInfoListener(infoListener: OnInfoListener?) {
        this.infoListener = infoListener
        audioEncoder?.setOnInfoListener(infoListener)
    }

    fun setOnErrorListener(errorListener: OnErrorListener?) {
        this.errorListener = errorListener
    }

    fun setOnDataListener(dataListener: OnDataListener?) {
        this.dataListener = dataListener
        audioEncoder?.setOnDataListener(dataListener)
    }

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
            audioEncoder = encoderFactory.create(config)
            audioEncoder?.setOnDataListener(dataListener)
            audioEncoder?.setOnInfoListener(infoListener)
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

        infoListener?.onInfo(INFO_AUDIO_START)
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
            errorListener?.onError(ERROR_STOP_ENCODER, e, null)
        }
        try {
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            errorListener?.onError(ERROR_STOP_AUDIORECORD, e, null)
        }
        infoListener?.onInfo(INFO_AUDIO_STOP)
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
        errorListener?.onError(error, extra, throwable)
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