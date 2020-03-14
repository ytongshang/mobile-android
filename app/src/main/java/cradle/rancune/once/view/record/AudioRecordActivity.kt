package cradle.rancune.once.view.record

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Bundle
import android.view.View
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.internal.utils.IOUtils
import cradle.rancune.internal.utils.T
import cradle.rancune.media.AudioConfig
import cradle.rancune.media.audiorecorder.AudioCallback
import cradle.rancune.media.audiorecorder.AudioWorker
import cradle.rancune.media.audiorecorder.utils.ADTSUtils
import cradle.rancune.once.R
import cradle.rancune.once.view.base.BaseActivity
import kotlinx.android.synthetic.main.once_activity_record_audio.*
import java.io.*
import java.nio.ByteBuffer

/**
 * Created by Rancune@126.com 2018/7/27.
 */
class AudioRecordActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "AudioRecordActivity"
    }

    private var worker: AudioWorker? = null
    private var outputStream: OutputStream? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.once_activity_record_audio)
        btn_start.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
        val config = AudioConfig()
        worker = AudioWorker(config, object :
            AudioCallback {
            override fun onState(state: Int) {
                when (state) {
                    AudioCallback.STATE_AUDIO_START -> {
                        isRecording = true
                    }
                    AudioCallback.STATE_AUDIO_STOP -> {
                        isRecording = false
                    }
                }
            }

            override fun onError(code: Int, e: Throwable?, extra: Any?) {
                AndroidLog.e(TAG, "error:$code", e)
                stopRecord()
            }

            override fun onFormatChanged(format: MediaFormat) {
            }

            override fun onOutputAvailable(info: MediaCodec.BufferInfo, buffer: ByteBuffer) {
                if (isRecording) {
                    if (outputStream == null) {
                        stopRecord()
                        return
                    }
                    //7为ADTS头部的大小
                    val chunkSize = info.size + 7
                    // 加入ADTS的文件头
                    val chunk = ByteArray(chunkSize)
                    ADTSUtils.addADTStoPacket(chunk, config.sampleRate, chunkSize)
                    // 读入原始的数据
                    buffer.position(info.offset)
                    buffer.limit(info.offset + info.size)
                    buffer.get(chunk, 7, info.size);
                    try {
                        outputStream!!.write(chunk, 0, chunkSize)
                    } catch (e: IOException) {
                        stopRecord()
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        IOUtils.closeQuietly(outputStream)
        super.onDestroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_start -> {
                startRecord()
            }
            R.id.btn_stop -> {
                stopRecord()
            }
        }
    }

    private fun startRecord() {
        AndPermission.with(this)
            .runtime()
            .permission(Permission.RECORD_AUDIO)
            .onGranted {
                val dir = this.getExternalFilesDir("audio")
                IOUtils.mkdirs(dir)
                val temp = File(dir, "${System.currentTimeMillis()}.aac")
                if (temp.exists()) {
                    temp.delete()
                }
                try {
                    outputStream = BufferedOutputStream(FileOutputStream(temp))
                } catch (e: FileNotFoundException) {
                    stopRecord()
                    return@onGranted
                }
                worker?.start()
            }
            .onDenied {
                T.showShort("语音权限被拒绝！！")
            }
            .start()
    }

    private fun stopRecord() {
        isRecording = false
        worker?.stop()
        outputStream?.let {
            it.flush()
            IOUtils.closeQuietly(it)
        }
        outputStream = null
    }
}