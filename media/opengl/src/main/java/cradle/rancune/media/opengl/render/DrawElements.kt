package cradle.rancune.media.opengl.render

import android.opengl.GLES30
import cradle.rancune.media.opengl.SimpleGlRender
import cradle.rancune.media.opengl.core.OpenGL
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Rancune@126.com 2020/10/7.
 */
class DrawElements : SimpleGlRender() {

    companion object {
        private val vertShader = """
        #version 300 es
        layout(location = 0) in vec4 aPosition;

        void main() {
            gl_Position = aPosition;
        }
    """.trimIndent()

        private val fragShader = """
        #version 300 es
        precision mediump float;
        out vec4 fragColor;
        
        void main () {
            fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        }
    """.trimIndent()
    }

    // 0, 1, 2, 3, 4
    //  这里顶点需要从0开始计算的话， GL_TRIANGLE_STRIP：有两种情况，
    // （1）当前顶点序号n是奇数时，三角形三个顶点的顺序是(n - 1, n - 2, n )。
    // （2）当前顶点序号n是偶数时，三角形三个顶点的顺序是(n - 2, n - 1, n)。
    // 所以生成的三角形是[v0,v1,v2], [v2,v1,v3],[v2, v3, v4]
    private val vertices = floatArrayOf(
        -0.5f, 0.5f,
        -0.25f, 0.0f,
        0.0f, 0.5f,
        0.25f, 0.0f,
        0.5f, 0.5f
    )
    private val vertexBuffer: FloatBuffer = OpenGL.createFloatBuffer(vertices)

    private val indices = byteArrayOf(
        0, 1, 2, 3, 4
    )
    private val indiceBuffer: ByteBuffer =
        ByteBuffer.allocateDirect(indices.size).put(indices).position(
            0
        ) as ByteBuffer

    private var glProgram: Int = 0

    /**
     * ！！！！！！！！！！！！！！！！！！！！！！
     * 这个方法的调用，必须在GL_THREAD,否则创建不了shader
     */
    override fun createShader() {
        super.createShader()
        glProgram = OpenGL.createProgram(vertShader, fragShader)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glUseProgram(glProgram)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, vertexBuffer)
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLE_STRIP,
            indices.size,
            GLES30.GL_UNSIGNED_BYTE,
            indiceBuffer
        )
        GLES30.glDisableVertexAttribArray(0)
    }
}