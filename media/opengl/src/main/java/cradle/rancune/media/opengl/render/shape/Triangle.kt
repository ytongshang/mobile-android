package cradle.rancune.media.opengl.render.shape

import android.opengl.GLES20
import android.opengl.GLES30
import cradle.rancune.media.opengl.SimpleGlRender
import cradle.rancune.media.opengl.core.OpenGL
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Rancune@126.com 2020/10/6.
 */
class Triangle : SimpleGlRender() {

    private val vertices = floatArrayOf(
        0.0f, 0.5f,    // top
        -0.5f, 0.0f,   // left bottom
        0.5f, 0.0f     // right bottom
    )

    // 黑色
    private val color = floatArrayOf(
        0.0f, 0.0f, 0.0f, 1.0f
    )

    private val vertShader = """
        #version 300 es
        layout(location = 0) in vec4 vPosition;

        void main() {
            gl_Position = vPosition;
        }
    """.trimIndent()

    private val fragShader = """
        #version 300 es
        precision mediump float;
        uniform vec4 uColor;
        out vec4 fragColor;
        
        void main () {
            fragColor = uColor;
        }
    """.trimIndent()

    private val vertexBuffer: FloatBuffer
    private var glProgram: Int = 0
    private var uColorLocation: Int = 0

    init {
        vertexBuffer = OpenGL.createFloatBuffer(vertices)
    }

    override fun createShader() {
        super.createShader()
        glProgram = OpenGL.createProgram(vertShader, fragShader)
        // 获得片段着色器中的uColor的Location
        uColorLocation = GLES30.glGetUniformLocation(glProgram, "uColor")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        // 白色
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        // 指定大小
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        // 使用OpenGL程序
        GLES30.glUseProgram(glProgram)
        // 指定颜色，4fv, 4个float的vec
        GLES30.glUniform4fv(uColorLocation, 1, color, 0)
        // 启用顶点数组，这里的0就是顶点着色器的layout限定符指定的值
        // layout(location = 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, vertexBuffer)
        GLES30.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 2)
        GLES30.glDisableVertexAttribArray(0)
    }
}