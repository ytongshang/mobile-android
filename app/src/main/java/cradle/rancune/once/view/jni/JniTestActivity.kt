package cradle.rancune.once.view.jni

import android.os.Bundle
import android.view.View
import cradle.rancune.internal.logger.AndroidLog
import cradle.rancune.media.ffmpeg.FFmpeg2
import cradle.rancune.once.R
import cradle.rancune.once.view.base.BaseActivity
import kotlinx.android.synthetic.main.once_activity_jni_test.*

/**
 * Created by Rancune@126.com 2020/3/30.
 */
class JniTestActivity : BaseActivity(), View.OnClickListener {

    companion object {
        const val TAG = "JniTestActivity"

        val ffmpeg = FFmpeg2()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.once_activity_jni_test)
        getCoder.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.getCoder -> {
                val log = ffmpeg.coder
                AndroidLog.d(TAG, log)
            }
        }
    }
}