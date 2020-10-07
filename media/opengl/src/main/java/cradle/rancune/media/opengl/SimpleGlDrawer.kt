package cradle.rancune.media.opengl

/**
 * Created by Rancune@126.com 2020/9/9.
 */
interface SimpleGlDrawer {

    fun createShader()

    fun onSurfaceCreated()

    fun onSurfaceChanged(width: Int, height: Int)

    fun onDrawFrame()

    fun destroy()
}