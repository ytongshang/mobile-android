package cradle.rancune.once.view.decode

import android.content.Context
import android.graphics.Point
import android.media.AudioFormat
import android.media.AudioManager
import android.media.MediaFormat
import android.os.*
import android.view.Surface
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.RelativeLayout
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.media.EncodedData
import cradle.rancune.media.OnDataListener
import cradle.rancune.media.OnErrorListener
import cradle.rancune.media.OnInfoListener
import cradle.rancune.media.audioplayer.AudioTrackPlayer
import cradle.rancune.media.decoder.MediaDecoder
import cradle.rancune.media.mediasource.FileExtractor
import cradle.rancune.once.Constant
import cradle.rancune.once.R
import cradle.rancune.once.view.base.BaseActivity
import kotlinx.android.synthetic.main.once_activity_mediacode_video.*
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Created by Rancune@126.com 2020/3/22.
 */
class MediaCodecVideoPlayActivity : BaseActivity(), Handler.Callback {

    companion object {
        const val TAG = "MediaCodecVideoPlayActivity"
        const val MSG_ERROR = 1
        const val MSG_INFO = 2
    }

    private var isSurfaceCreated: Boolean = false
    private val fileName = "westworld.mp4"

    private var audioSource: FileExtractor? = null
    private var audioDecoder: MediaDecoder? = null
    private var videoSource: FileExtractor? = null
    private var videoDecoder: MediaDecoder? = null

    private var audioPlayer: AudioTrackPlayer? = null

    @Volatile
    private var isDecoding = false

    private val handler = Handler(Looper.getMainLooper(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.once_activity_mediacode_video)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                isSurfaceCreated = true
                isDecoding = true
                startPlay(holder?.surface!!)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                isSurfaceCreated = false
                isDecoding = false
            }

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
            }
        })
    }

    override fun onDestroy() {
        isDecoding = false
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun startPlay(sf: Surface) {
        thread{
            val f = File(getExternalFilesDir(Constant.VIDEO_FILE), fileName)
            videoSource = FileExtractor(f.absolutePath)
            videoSource?.prepare()
            videoSource?.findTrack("video")
            val format = videoSource?.mediaFormat ?: return@thread
            // 一般可以在这里使用新的输出格式，比如压缩就可以在这里做
            videoDecoder = MediaDecoder(format, videoSource!!, sf)
            videoDecoder?.prepare()
            videoDecoder?.start()
            val startTime = SystemClock.uptimeMillis()
            videoDecoder?.setOnInfoListener(object : OnInfoListener {
                override fun onInfo(what: Int, extra: Any?) {
                    val msg = handler.obtainMessage(MSG_INFO)
                    msg.arg1 = what
                    handler.sendMessage(msg)
                }
            })
            videoDecoder?.setOnErrorListener(object : OnErrorListener {
                override fun onError(what: Int, extra: Any?, throwable: Throwable?) {
                    AndroidLog.e(TAG, "videoDEcoder error, code:$what, extra:$extra", throwable)
                }
            })
            videoDecoder?.setOnDataListener(object : OnDataListener {
                override fun onOutputAvailable(data: EncodedData) {
                    val pts = data.bufferInfo?.presentationTimeUs ?: return
                    val time = (SystemClock.uptimeMillis() - startTime) * 1000
                    if (pts > time) {
                        // 同步到外部时间
                        TimeUnit.MICROSECONDS.sleep(pts - time)
                    }
                }
            })

            while (isDecoding) {
                AndroidLog.d("Rancune", "while:" + Thread.currentThread().name)
                videoDecoder?.offerDecoder()
                videoDecoder?.drainDecoder()
            }

            videoSource?.close()
            videoDecoder?.release()
            videoDecoder = null
        }
        thread(start = true) {
            val f = File(getExternalFilesDir(Constant.VIDEO_FILE), fileName)
            audioSource = FileExtractor(f.absolutePath)
            audioSource?.prepare()
            audioSource?.findTrack("audio")
            val format = audioSource?.mediaFormat ?: return@thread
            audioDecoder = MediaDecoder(format, audioSource!!, null)
            audioDecoder?.setOnInfoListener(object : OnInfoListener {
                override fun onInfo(what: Int, extra: Any?) {
                    if (what == MediaDecoder.INFO_OUTPUT_MEDIAFORMAT_CHANGED) {
                        if (audioPlayer == null) {
                            val config = AudioTrackPlayer.Config()
                            val outputFormat = audioDecoder!!.outputFormat!!
                            config.streamStype = AudioManager.STREAM_MUSIC
                            // 采样率
                            config.sampleRate = outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            // channel
                            val channelCount =
                                outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            config.channel =
                                if (channelCount > 1) {
                                    AudioFormat.CHANNEL_OUT_STEREO
                                } else {
                                    AudioFormat.CHANNEL_OUT_MONO
                                }
                            // encoding bits
                            val encoding = outputFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
                            val encodingFormat =
                                if (encoding == 2) AudioFormat.ENCODING_PCM_16BIT else AudioFormat.ENCODING_PCM_8BIT
                            config.audioEncodingFormat = encodingFormat
                            audioPlayer = AudioTrackPlayer(config)
                            audioPlayer?.prepare()
                            audioPlayer?.start()
                        }
                    }
                }
            })
            audioDecoder?.setOnErrorListener(object : OnErrorListener {
                override fun onError(what: Int, extra: Any?, throwable: Throwable?) {
                    AndroidLog.e(TAG, "audioDecoder error, code:$what, extra:$extra", throwable)
                }
            })
            audioDecoder?.setOnDataListener(object : OnDataListener {
                override fun onOutputAvailable(data: EncodedData) {
                    audioPlayer?.offer(data.byteArray!!, data.offset, data.size)
                }
            })

            audioDecoder?.prepare()
            audioDecoder?.start()

            while (isDecoding) {
                audioDecoder?.offerDecoder()
                audioDecoder?.drainDecoder()
            }

            audioSource?.close()
            audioDecoder?.release()
            audioDecoder = null

            audioPlayer?.release()
            audioPlayer = null
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_INFO -> {
                when (msg.arg1) {
                    MediaDecoder.INFO_OUTPUT_MEDIAFORMAT_CHANGED -> {
                        processVideoSize()
                    }
                }
            }
        }
        return true
    }

    private fun processVideoSize() {
        if (!isSurfaceCreated) {
            return
        }
        val coder = videoDecoder ?: return
        val output = coder.outputFormat ?: return
        val w = output.getInteger(MediaFormat.KEY_WIDTH)
        val h = output.getInteger(MediaFormat.KEY_HEIGHT)
        if (w > 0 && h > 0) {
            AndroidLog.d(TAG, "onVideoSizeChanged,width:$w, height:$h")
            val pt = getScreenSize()
            val params = surfaceView.layoutParams as RelativeLayout.LayoutParams
            val x: Int = pt.x.coerceAtMost(pt.y)
            val y: Int = pt.x.coerceAtLeast(pt.y)
            if (h * x / w <= y) {
                params.width = x
                params.height = h * x / w
            } else {
                when {
                    w * y / h > x -> {
                        params.width = w * y / h
                        params.height = y
                    }
                    h * x / w > y -> {
                        params.width = x
                        params.height = h * x / w
                    }
                    else -> {
                        params.width = x
                        params.height = y
                    }
                }
            }
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            surfaceView.layoutParams = params
            surfaceView.requestLayout()
        }
    }

    private fun getScreenSize(): Point {
        val pt = Point()
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getSize(pt)
        return pt
    }
}