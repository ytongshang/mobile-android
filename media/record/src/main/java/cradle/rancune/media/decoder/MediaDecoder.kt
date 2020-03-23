package cradle.rancune.media.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.view.Surface
import cradle.rancune.media.*


/**
 * Created by Rancune@126.com 2020/3/16.
 */
class MediaDecoder(
    private val mediaFormat: MediaFormat,
    private val source: MediaSource,
    private val surface: Surface? = null
) {

    companion object {
        const val TIMEOUTUS = 1000L

        const val ERROR_INVALID_MEDIAFORMAT = 1
        const val ERROR_CREATE_MEDIACODEC = 2
        const val ERROR_START_DECODER = 3
        const val ERROR_STOP_DECODER = 4

        const val INFO_OUTPUT_MEDIAFORMAT_CHANGED = 1
    }

    private var decoder: MediaCodec? = null
    private var isStopped: Boolean = true
    private var isEndOfStream: Boolean = false
    var outputFormat: MediaFormat? = null
        private set

    private var errorListener: OnErrorListener? = null
    private var infoListener: OnInfoListener? = null
    private var dataListener: OnDataListener? = null

    fun setOnInfoListener(infoListener: OnInfoListener?) {
        this.infoListener = infoListener
    }

    fun setOnErrorListener(errorListener: OnErrorListener?) {
        this.errorListener = errorListener
    }

    fun setOnDataListener(dataListener: OnDataListener?) {
        this.dataListener = dataListener
    }

    fun prepare() {
        val mine = mediaFormat.getString(MediaFormat.KEY_MIME)
        if (mine.isNullOrEmpty()) {
            onError(ERROR_INVALID_MEDIAFORMAT, "MediaFormat: $mediaFormat")
            return
        }
        try {
            decoder = MediaCodec.createDecoderByType(mine)
            decoder?.configure(mediaFormat, surface, null, 0)
        } catch (e: Exception) {
            onError(ERROR_CREATE_MEDIACODEC, "", e)
            return
        }
    }

    fun start() {
        try {
            isStopped = false
            decoder?.start()
        } catch (e: Exception) {
            onError(ERROR_START_DECODER, "", e)
        }
    }

    fun stop() {
        try {
            isStopped = true
            decoder?.stop()
        } catch (e: Exception) {
            errorListener?.onError(ERROR_STOP_DECODER, "", e)
        }
    }

    fun release() {
        decoder?.release()
        decoder = null
    }

    fun offerDecoder() {
        if (isStopped) {
            return
        }
        val coder = decoder ?: return
        if (isEndOfStream) {
            return
        }
        val index = coder.dequeueInputBuffer(TIMEOUTUS)
        if (index > 0) {
            val buffer = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                coder.getInputBuffer(index)
            } else {
                val b = coder.inputBuffers[index]
                b?.clear()
                b
            }) ?: return
            val readSize = source.read(buffer)
            if (readSize < 0) {
                isEndOfStream = true
                coder.queueInputBuffer(
                    index, 0, 0, source.presentationTimeUs(),
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
            } else {
                coder.queueInputBuffer(
                    index, 0, readSize, source.presentationTimeUs(),
                    0
                )
            }
        }
    }

    fun drainDecoder() {
        val coder = decoder ?: return
        loop@ while (true) {
            val bufferInfo = MediaCodec.BufferInfo()
            val index = coder.dequeueOutputBuffer(bufferInfo, TIMEOUTUS)
            when {
                index == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    break@loop
                }
                index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    outputFormat = coder.outputFormat
                    infoListener?.onInfo(INFO_OUTPUT_MEDIAFORMAT_CHANGED)
                }
                index < 0 -> {
                }
                else -> {
                    if (surface != null) {
                        // 这里可能要做延迟的
                        dataListener?.onOutputAvailable(EncodedData(bufferInfo = bufferInfo))
                        coder.releaseOutputBuffer(index, true)
                        // output有surface时，getOutputBuffer获取为空
                    } else {
                        dataListener?.let {
                            val buffer =
                                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    coder.getOutputBuffer(index)
                                } else {
                                    val b = coder.outputBuffers[index]
                                    b?.clear()
                                    b
                                }) ?: return
                            buffer.position(bufferInfo.offset)
                            buffer.limit(bufferInfo.offset + bufferInfo.size)
                            val data = EncodedData()
                            data.offset = 0
                            data.size = bufferInfo.size
                            data.byteArray = ByteArray(data.size)
                            buffer.get(data.byteArray!!, data.offset, data.size)
                            data.bufferInfo = bufferInfo
                            it.onOutputAvailable(data)
                        }
                    }
                }
            }
        }
    }

    private fun onError(what: Int, extra: Any? = null, throwable: Throwable? = null) {
        errorListener?.onError(what, extra, throwable)
    }
}