package cradle.rancune.media

import java.nio.ByteBuffer

/**
 * Created by Rancune@126.com 2020/3/21.
 */
interface MediaSource {

    fun read(buffer: ByteBuffer): Int

    fun presentationTimeUs(): Long

    fun close()
}