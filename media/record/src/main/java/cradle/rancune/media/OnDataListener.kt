package cradle.rancune.media

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * Created by Rancune@126.com 2020/3/22.
 */
interface OnDataListener {
    fun onOutputAvailable(buffer: ByteBuffer?, info: MediaCodec.BufferInfo)
}