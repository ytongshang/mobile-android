package cradle.rancune.once.view.decode

import android.content.Context
import android.graphics.Point
import android.media.MediaFormat
import android.os.*
import android.view.Surface
import android.view.SurfaceHolder
import android.view.WindowManager
import android.widget.RelativeLayout
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.media.OnInfoListener
import cradle.rancune.media.decoder.MediaDecoder
import cradle.rancune.media.mediasource.FileExtractor
import cradle.rancune.once.Constant
import cradle.rancune.once.R
import cradle.rancune.once.view.base.BaseActivity
import kotlinx.android.synthetic.main.once_activity_mediacode_video.*
import java.io.File
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
    private var mediaSource: FileExtractor? = null
    private var decoder: MediaDecoder? = null
    private val fileName = "westworld.mp4"
    private val lock = Any()

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
        super.onDestroy()
        mediaSource?.close()
        mediaSource = null
        decoder?.release()
        decoder = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun startPlay(sf: Surface) {
        thread {
            val f = File(getExternalFilesDir(Constant.VIDEO_FILE), fileName)
            mediaSource = FileExtractor(f.absolutePath)
            mediaSource?.prepare()
            mediaSource?.findTrack("video")
            val format = mediaSource?.mediaFormat ?: return@thread
            // 一般可以在这里使用新的输出格式，比如压缩就可以在这里做
            decoder = MediaDecoder(format, mediaSource!!, sf)
            decoder?.prepare()
            decoder?.start()
            decoder?.onInfoListener = object : OnInfoListener {
                override fun onInfo(what: Int, extra: Any?) {
                    val msg = handler.obtainMessage(MSG_INFO)
                    msg.arg1 = what
                    handler.sendMessage(msg)
                }
            }

            while (isDecoding) {
                decoder?.offerDecoder()
                decoder?.drainDecoder()
            }
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
        val coder = decoder ?: return
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