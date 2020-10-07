package cradle.rancune.media.opengl.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.widget.SeekBar
import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.internal.utils.AppUtils
import cradle.rancune.media.opengl.R
import cradle.rancune.media.opengl.render.Saturation
import kotlinx.android.synthetic.main.opengl_activity_saturation.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Rancune@126.com 2020/10/7.
 */
class OpenglSaturationActivity : BaseActivity() {

    private val bitmap: Bitmap
    private var saturation = 0f

    init {
        val options = BitmapFactory.Options()
        options.inScaled = false
        bitmap = BitmapFactory.decodeResource(
            AppUtils.application.resources,
            R.drawable.opengl_lenna,
            options
        )
        saturation = 0f
    }

    private var saturationRender: Saturation = Saturation()

    override fun initView() {
        setContentView(R.layout.opengl_activity_saturation)
        seekbarSaturation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                saturationRender.setSaturation(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    override fun initData() {
        glSurfaceView.setEGLContextClientVersion(3)
        val render = object : GLSurfaceView.Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                saturationRender.createShader()
                saturationRender.onSurfaceCreated(gl, config)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                saturationRender.onSurfaceChanged(gl, width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                saturationRender.onDrawFrame(gl)
            }
        }
        glSurfaceView.setRenderer(render)
        saturationRender.setBitmap(bitmap)
        saturationRender.setSaturation(saturation)
    }

    override fun onRestart() {
        super.onRestart()
        glSurfaceView?.onResume()
    }

    override fun onPause() {
        glSurfaceView?.onPause()
        super.onPause()
    }
}