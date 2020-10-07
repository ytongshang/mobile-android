package cradle.rancune.media.opengl

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Rancune@126.com 2020/10/7.
 */
class GLRenderWrapper(private val clazz: Class<out SimpleGlDrawer>) : GLSurfaceView.Renderer {
    private var render: SimpleGlDrawer? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig) {
        render = try {
            clazz.newInstance()
        } catch (e: Exception) {
            null
        }
        // 在GL_THREAD创建shader
        render?.createShader()
        render?.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        render?.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        render?.onDrawFrame()
    }
}