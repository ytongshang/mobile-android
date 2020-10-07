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

    private val vertexBuffer: FloatBuffer = OpenGL.createFloatBuffer(vertices)
    private var glProgram: Int = 0
    private var uColorLocation: Int = 0

    /**
     * ！！！！！！！！！！！！！！！！！！！！！！
     * 这个方法的调用，必须在GL_THREAD,否则创建不了shader
     */
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
        // 启用顶点属性数组，这里的0就是顶点着色器的layout限定符指定的值
        // layout(location = 0)
        GLES30.glEnableVertexAttribArray(0)
        // index 顶点属性的location
        // size, 每一个顶点属性的分量数量，这里的坐标是2维的xy,所以是2
        // type GL_BYTE, GL_UNSIGNED_BYTE,GL_SHORT, GL_UNSIGNED_SHORT, GL_INT, GL_UNSIGNED_INT
        // 并且opengles 3.0，新增了一个GL_HALF_SHORT,不过java中一般用不到
        // nomalized, 表示非浮点数据格式类型在转换为浮点值时是否应当规范化
        // stride,指定顶点索引I和I+1表示的顶点的数据之间的位移，这里float是4个byte,2个float,所以是4*2
        // ptr,缓冲区对象
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, vertexBuffer)
        // mode,指定要渲染的图元，
        // GL_POINTS, GL_LINES, GL_LINE_STRIP, GL_LINE_LOOP, GL_TRIANGLES,GL_TRIANGLES_STRIP,GL_TRIANGLES_FAN
        // first, 指定启用的顶点数组中起始顶点的索引
        // count,指定要绘制的顶点数量
        GLES30.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 2)
        // 禁用顶点数组0
        GLES30.glDisableVertexAttribArray(0)
    }
}