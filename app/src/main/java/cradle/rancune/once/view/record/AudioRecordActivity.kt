package cradle.rancune.once.view.record

import android.media.MediaFormat
import android.os.Bundle
import android.view.View
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.internal.utils.IOUtils
import cradle.rancune.internal.utils.T
import cradle.rancune.media.EncodedData
import cradle.rancune.media.AudioEncoder
import cradle.rancune.media.audiorecorder.AudioRecordWorker
import cradle.rancune.media.audiorecorder.utils.ADTSUtils
import cradle.rancune.once.Constant
import cradle.rancune.once.R
import cradle.rancune.once.view.base.BaseActivity
import kotlinx.android.synthetic.main.once_activity_record_audio.*
import java.io.*

/**
 * Created by Rancune@126.com 2018/7/27.
 */
class AudioRecordActivity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "AudioRecordActivity"
    }

    private var worker: AudioRecordWorker? = null
    private var outputStream: OutputStream? = null
    private var isRecording = false
    private var addADTS = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.once_activity_record_audio)
        btn_start.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
        radioGroup.setOnCheckedChangeListener { _, id ->
            addADTS = id == R.id.adts
        }
        val config = AudioRecordWorker.Config()
        worker = AudioRecordWorker(config, object : AudioRecordWorker.Listener {
            override fun onState(state: Int) {
                when (state) {
                    AudioRecordWorker.STATE_AUDIO_START -> {
                        isRecording = true
                    }
                    AudioRecordWorker.STATE_AUDIO_STOP -> {
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

            override fun onOutputAvailable(data: EncodedData) {
                if (isRecording) {
                    if (outputStream == null) {
                        stopRecord()
                        return
                    }
                    val ori = data.byteArray ?: return
                    if (addADTS) {
                        // 7为ADTS头部的大小
                        val dstSize = data.size + 7
                        val dst = ByteArray(dstSize)
                        // 加入ADTS的文件头
                        ADTSUtils.addADTStoPacket(dst, config.sampleRate, dstSize)
                        // 原始数据
                        System.arraycopy(ori, data.offset, dst, 7, data.size)
                        try {
                            outputStream!!.write(dst, 0, dstSize)
                        } catch (e: IOException) {
                            stopRecord()
                        }
                    } else {
                        try {
                            outputStream!!.write(ori, data.offset, data.size)
                        } catch (e: IOException) {
                            stopRecord()
                        }
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
                val dir = this.getExternalFilesDir(Constant.AUDIO_FILE)
                IOUtils.mkdirs(dir)
                val suffix: String
                val factory: AudioEncoder.Factory
                if (addADTS) {
                    suffix = ".aac"
                    factory = AudioEncoder.MEDIA_CODEC
                } else {
                    suffix = ".pcm"
                    factory = AudioEncoder.PCM
                }
                val temp = File(dir, "test${suffix}")
                if (temp.exists()) {
                    temp.delete()
                }
                try {
                    outputStream = BufferedOutputStream(FileOutputStream(temp))
                } catch (e: FileNotFoundException) {
                    stopRecord()
                    return@onGranted
                }
                worker?.setEncoderFactory(factory)
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