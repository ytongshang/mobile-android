@file:Suppress("FunctionName")

package cradle.rancune.media.audiorecorder

import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Process
import cradle.rancune.media.AudioConfig
import java.lang.ref.WeakReference
import kotlin.math.ceil

/**
 * Created by Rancune@126.com 2020/3/3.
 */
class AudioWorker(private val config: AudioConfig, private val listener: AudioCallback) : Runnable {

    companion object {
        const val TAG = "AudioWorker"

        const val MSG_START = 1
        const val MSG_STOP = 2
        const val MSG_FRAME = 3
    }

    private val lock = Object()
    private var handler: Handler? = null
    private var ready = false
    private var running = false
    private var stopped = false

    private var audioRecord: AudioRecord? = null
    private var audioEncoder: AudioEncoder? = null

    fun start() {
        synchronized(lock) {
            if (running) {
                return
            }
            running = true
            Thread(this,
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
        val format: Int = config.format
        val samplePerFrame: Int = config.samplePerFrame

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, format)
        if (minBufferSize <= 0) {
            if (handleFailure(AudioCallback.ERROR_AUDIO_MIN_BUFFER)) {
                return
            }
        }
        val bufferSize = ceil(minBufferSize.toFloat() / samplePerFrame.toFloat()).toInt() * samplePerFrame
        config.minBufferSize = bufferSize

        try {
            audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, format, bufferSize)
        } catch (e: Exception) {
            if (handleFailure(AudioCallback.ERROR_AUDIO_CREATE_RECORD)) {
                return
            }
        }

        try {
            audioEncoder =
                AudioEncoder(config, listener)
        } catch (e: Exception) {
            if (handleFailure(AudioCallback.ERROR_AUDIO_CREATE_ENCODER)) {
                return
            }
        }

        try {
            audioRecord?.startRecording()
        } catch (e: Exception) {
            if (handleFailure(AudioCallback.ERROR_AUDIO_START_RECORD)) {
                return
            }
        }

        try {
            audioEncoder?.start()
        } catch (e: Exception) {
            if (handleFailure(AudioCallback.ERROR_AUDIO_START_ENCODER)) {
                return
            }
        }

        listener.onState(AudioCallback.STATE_AUDIO_START)
        handler?.sendEmptyMessage(MSG_FRAME)
    }

    private fun _frame(endOfStream: Boolean = false) {
        try {
            // 生产
            audioEncoder?.offer(audioRecord!!, endOfStream)
        } catch (e: Exception) {
            if (handleFailure(AudioCallback.ERROR_AUDIO_ENCODER_OFFER)) {
                return
            }
        }
        try {
            // 消费
            audioEncoder?.consume(endOfStream)
        } catch (e: Exception) {
            if (handleFailure(AudioCallback.ERROR_AUDIO_ENCODER_CONSUME)) {
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
            listener.onError(AudioCallback.ERROR_AUDIO_STOP_ENCODER, e, null)
        }
        try {
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            listener.onError(AudioCallback.ERROR_AUDIO_STOP_RECORD, e, null)
        }
        listener.onState(AudioCallback.STATE_AUDIO_STOP)
        ready = false
        running = false
        stopped = false
        handler?.looper?.quit()
        handler = null
    }

    private fun handleFailure(error: Int, throwable: Throwable? = null, extra: Any? = null): Boolean {
        listener.onError(error, throwable, extra)
        return true
    }

    class AudioHandler(worker: AudioWorker) : Handler() {
        private val ref = WeakReference<AudioWorker>(worker)

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