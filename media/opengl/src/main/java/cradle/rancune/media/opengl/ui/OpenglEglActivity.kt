package cradle.rancune.media.opengl.ui

import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.internal.logger.L
import cradle.rancune.media.opengl.R
import cradle.rancune.media.opengl.core.EglHelper
import cradle.rancune.media.opengl.render.Sample2D
import kotlinx.android.synthetic.main.opengl_activity_egl.*

/**
 * Created by Rancune@126.com 2020/10/7.
 */
class OpenglEglActivity : BaseActivity(), SurfaceHolder.Callback {

    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler

    // eglCore
    private val eglHelper: EglHelper = EglHelper()

    // 创建一个Sample2D
    private val drawer: Sample2D = Sample2D()

    override fun initView() {
        setContentView(R.layout.opengl_activity_egl)
        surfaceView.holder.addCallback(this)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.setTitle(R.string.opengl_activity_opengl9)
    }

    override fun initData() {
        // Opengl的所有操作需要在一个线程中，这里使用HandlerThread
        handlerThread = HandlerThread("OpenglEglActivity Egl Thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
        super.onDestroy()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        handler.post {
            L.d(TAG, "surfaceCreated")
            // 先创建好egl环境
            eglHelper.init(null, EglHelper.FLAG_TRY_GLES3)
            // 创建windowSurface, 这样egl绘制的内容会到surfaceView的surface上
            eglHelper.createWindowSurface(holder)
            // egl环境创建好后，才能够创建shader
            drawer.createShader()
            drawer.onSurfaceCreated()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        handler.post {
            L.d(TAG, "surfaceChanged")
            drawer.onSurfaceChanged(width, height)
            drawer.onDrawFrame()
            // opengles 3.0只支持EGL_BACK_BUFFER,所以这里必须调用一下这个方法
            // 交换一下缓冲区，这样上面绘制的内容才会显示到屏幕上
            eglHelper.swapBuffers()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        handler.post {
            L.d(TAG, "surfaceDestroyed")
            // opengl相关内容销毁
            drawer.destroy()
            // 销毁egl
            eglHelper.destroy()
        }
    }
}