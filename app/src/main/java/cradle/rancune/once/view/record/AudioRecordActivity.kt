package cradle.rancune.once.view.record

import android.view.View
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission
import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.internal.utils.IOUtils
import cradle.rancune.internal.utils.T
import cradle.rancune.media.*
import cradle.rancune.media.audiorecorder.AudioRecordWorker
import cradle.rancune.media.audiorecorder.utils.ADTSUtils
import cradle.rancune.once.Constant
import cradle.rancune.once.R
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

    @Volatile
    private var isRecording = false
    private var addADTS = true

    override fun initView() {
        setContentView(R.layout.once_activity_record_audio)
        btn_start.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
        radioGroup.setOnCheckedChangeListener { _, id ->
            addADTS = id == R.id.adts
        }
    }

    override fun initData() {
        val config = AudioConfig()
        worker = AudioRecordWorker(config)
        worker?.setOnInfoListener(object : OnInfoListener {
            override fun onInfo(what: Int, extra: Any?) {
                when (what) {
                    AudioRecordWorker.INFO_AUDIO_START -> {
                        isRecording = true
                    }
                    AudioRecordWorker.INFO_AUDIO_STOP -> {
                        isRecording = false
                    }
                }
            }
        })
        worker?.setOnErrorListener(object : OnErrorListener {
            override fun onError(what: Int, extra: Any?, throwable: Throwable?) {
            }
        })
        worker?.setOnDataListener(object : OnDataListener {
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