package cradle.rancune.media.mediasource

import android.media.MediaExtractor
import android.media.MediaFormat
import cradle.rancune.media.MediaSource
import java.nio.ByteBuffer

/**
 * Created by Rancune@126.com 2020/3/15.
 */
open class FileExtractor(private val path: String) : MediaSource {

    private var extractor: MediaExtractor? = null

    private var track = -1
    private var sampleTime: Long = -1
    private var isEndOfStream: Boolean = false
    var mediaFormat: MediaFormat? = null
        private set

    fun prepare() {
        extractor = MediaExtractor()
        extractor!!.setDataSource(path)
    }

    override fun read(buffer: ByteBuffer): Int {
        if (track == -1) {
            return -1
        }
        if (isEndOfStream) {
            return -1
        }
        buffer.clear()
        val readSampleCount = extractor!!.readSampleData(buffer, 0)
        if (readSampleCount < 0) {
            isEndOfStream = true
            return -1
        }
        //记录当前帧的时间戳
        sampleTime = extractor!!.sampleTime
        extractor!!.advance()
        return readSampleCount
    }

    override fun presentationTimeUs(): Long {
        return sampleTime
    }

    override fun close() {
        track = -1
        mediaFormat
        extractor?.release()
        extractor = null
    }

    fun findTrack(trackPrefix: String): Boolean {
        val trackCount = extractor?.trackCount ?: 0
        for (i in 0 until trackCount) {
            val format = extractor!!.getTrackFormat(i)
            val mine = format.getString(MediaFormat.KEY_MIME)
            if (!mine.isNullOrEmpty() && mine.startsWith(trackPrefix)) {
                track = i
                mediaFormat = format
                break
            }
        }
        if (track >= 0) {
            extractor?.selectTrack(track)
            return true
        }
        return false
    }
}