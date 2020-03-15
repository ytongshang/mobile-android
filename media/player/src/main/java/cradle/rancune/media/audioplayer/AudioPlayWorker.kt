@file:Suppress("FunctionName")

package cradle.rancune.media.audioplayer

import android.media.*
import android.os.*
import cradle.rancune.internal.logger.AndroidLog
import java.lang.ref.WeakReference


/**
 * Created by Rancune@126.com 2020/3/14.
 */
class AudioPlayWorker(private val config: AudioPlayConfig) : Runnable {

    companion object {
        const val TAG = "AudioPlayWorker"

        const val MSG_START = 1
        const val MSG_FRAME = 2
        const val MSG_RESUME = 3
        const val MSG_PAUSE = 4
        const val MSG_RELEASE = 5
    }

    private var audioTrack: AudioTrack? = null
    private var listener: AudioPlayCallback? = null

    private val lock = Object()
    private var handler: Handler? = null
    private var ready = false
    private var running = false
    private var paused = false
    private var released = false

    fun start() {
        synchronized(lock) {
            if (running) {
                return
            }
            running = true
            Thread(this, TAG).start()
            while (!ready) {
                try {
                    lock.wait()
                } catch (ignored: InterruptedException) {
                }
            }
        }
        handler?.sendEmptyMessage(MSG_START)
    }

    fun write(byteArray: ByteArray, offsetInBytes: Int, sizeInBytes: Int) {
        synchronized(lock) {
            if (!running) {
                return
            }
        }
        val message = handler!!.obtainMessage(MSG_FRAME)
        message.obj = byteArray
        message.arg1 = offsetInBytes
        message.arg2 = sizeInBytes
        handler?.sendMessage(message)
    }

    fun pause() {
        synchronized(lock) {
            if (!running) {
                return
            }
            if (paused) {
                return
            }
            paused = true
        }
        handler?.sendEmptyMessage(MSG_PAUSE)
    }

    fun resume() {
        synchronized(lock) {
            if (!running) {
                return
            }
            if (!paused) {
                return
            }
            paused = false
        }
        handler?.sendEmptyMessage(MSG_PAUSE)
    }

    fun release() {
        synchronized(lock) {
            if (!running) {
                return
            }
            if (released) {
                return
            }
            released = true
        }
        handler?.sendEmptyMessage(MSG_RELEASE)
    }

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        Looper.prepare()
        synchronized(lock) {
            handler = H(this)
            ready = true
            lock.notifyAll()
        }
        Looper.loop()
    }

    private fun _start() {
        val sampleRate: Int = config.sampleRate
        val channelConfig: Int = config.channel
        val encodingFormat: Int = config.encodingFormat
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, encodingFormat)
        AndroidLog.d(TAG, "$minBufferSize")
        if (minBufferSize <= 0) {
            handleFailure(AudioPlayCallback.ERROR_AUDIOTRACK_MIN_BUFFER, null, null)
            return
        }

        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                audioTrack = AudioTrack(
                    config.streamStype,
                    sampleRate,
                    channelConfig,
                    encodingFormat,
                    minBufferSize,
                    AudioTrack.MODE_STREAM
                )
            } else {
                val attributes = AudioAttributes.Builder()
                    .setLegacyStreamType(config.streamStype)
                    .build()
                val format = AudioFormat.Builder()
                    .setSampleRate(config.sampleRate)
                    .setEncoding(config.encodingFormat)
                    .setChannelMask(config.channel)
                    .build()
                audioTrack = AudioTrack(
                    attributes, format, minBufferSize, AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
            }
        } catch (e: Exception) {
            if (handleFailure(AudioPlayCallback.ERROR_AUDIOTRACK_CREATED, e)) {
                return
            }
        }
        audioTrack?.play()
    }

    fun _frame(data: Any, offsetInBytes: Int, sizeInBytes: Int) {
        if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
            when (data) {
                is ByteArray -> {
                    val size = audioTrack!!.write(data, offsetInBytes, sizeInBytes)
                    when {
                        size == AudioRecord.ERROR_INVALID_OPERATION -> {
                            AndroidLog.d("Rancune", "error: ERROR_INVALID_OPERATION")
                        }
                        size == AudioRecord.ERROR_BAD_VALUE -> {
                            AndroidLog.d("Rancune", "error: ERROR_BAD_VALUE")
                        }
                        size == AudioRecord.ERROR_DEAD_OBJECT -> {
                            AndroidLog.d("Rancune", "error: ERROR_DEAD_OBJECT")
                        }
                    }
                }
            }
        }
    }

    fun _pause() {
        val state = audioTrack?.state
        if (state == AudioTrack.STATE_INITIALIZED) {
            audioTrack?.pause()
        }
    }

    fun _resume() {
        val state = audioTrack?.state
        if (state == AudioTrack.STATE_INITIALIZED) {
            audioTrack?.play()
        }
    }

    fun _release() {
        running = false
        ready = false
        running = false
        released = false
        try {
            if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack?.stop()
                audioTrack?.flush()
            }
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            listener?.onError(AudioPlayCallback.ERROR_AUDIOTRACK_RELEASE, e)
        }
        handler?.looper?.quit()
        handler = null
    }

    private fun handleFailure(
        error: Int,
        throwable: Throwable? = null,
        extra: Any? = null
    ): Boolean {
        listener?.onError(error, throwable, extra)
        return true
    }

    private class H(worker: AudioPlayWorker) : Handler() {
        private val ref = WeakReference<AudioPlayWorker>(worker)

        override fun handleMessage(msg: Message) {
            val workder = ref.get() ?: return
            when (msg.what) {
                MSG_START -> {
                    workder._start()
                }
                MSG_FRAME -> {
                    val data = msg.obj
                    val offset = msg.arg1
                    val size = msg.arg2
                    workder._frame(data, offset, size)
                }
                MSG_PAUSE -> {
                    workder._pause()
                }
                MSG_RESUME -> {
                    workder._resume()
                }
                MSG_RELEASE -> {
                    workder._release()
                }
            }
        }
    }
}