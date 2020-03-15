package cradle.rancune.media

import android.media.MediaCodec

/**
 * Created by Rancune@126.com 2020/3/15.
 */
class EncodedData {
    var byteArray: ByteArray? = null
    var offset: Int = 0
    var size: Int = 0
    var bufferInfo: MediaCodec.BufferInfo? = null
}