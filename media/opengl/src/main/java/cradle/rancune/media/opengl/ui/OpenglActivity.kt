package cradle.rancune.media.opengl.ui

import android.opengl.GLSurfaceView
import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.media.opengl.R
import cradle.rancune.media.opengl.SimpleGlRender
import cradle.rancune.media.opengl.render.shape.Triangle
import kotlinx.android.synthetic.main.opengl_activity_opengl.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Rancune@126.com 2020/10/6.
 */
class OpenglActivity : BaseActivity(), GLSurfaceView.Renderer {

    private lateinit var render: SimpleGlRender

    override fun initView() {
        setContentView(R.layout.opengl_activity_opengl)
    }

    override fun initData() {
        glSurfaceView.setEGLContextClientVersion(3)
        render = getRender()
        glSurfaceView.setRenderer(this)
    }

    override fun onStart() {
        super.onStart()
        glSurfaceView?.onResume()
    }

    override fun onStop() {
        glSurfaceView?.onPause()
        super.onStop()
    }

    fun getRender(): SimpleGlRender {
        return Triangle()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 必须在GL_THREAD创建shader
        render.createShader()
        render.onSurfaceCreated(gl, config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        render.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        render.onDrawFrame(gl)
    }
}