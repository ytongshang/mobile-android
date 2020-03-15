package cradle.rancune.media

import android.media.AudioFormat
import android.media.AudioRecord

/**
 * Created by Rancune@126.com 2018/7/24.
 * https://zhuanlan.zhihu.com/p/20627525
 * https://www.jianshu.com/p/c398754e5984
 * https://blog.csdn.net/charleslei/article/details/53099428
 */
class AudioConfig {
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
