package cradle.rancune.media.opengl

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Rancune@126.com 2020/9/9.
 */
open class SimpleGlRender : GLSurfaceView.Renderer {

    open fun createShader() {
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    override fun onDrawFrame(gl: GL10?) {
    }

}