package cradle.rancune.media.opengl.render

import android.opengl.GLES30
import android.opengl.Matrix
import cradle.rancune.media.opengl.SimpleGlRender
import cradle.rancune.media.opengl.core.OpenGL
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Rancune@126.com 2020/10/7.
 */
class Shape : SimpleGlRender() {

    // (-1,1) 0		(1,1) 3
    //	 ____________
    //  |			 |
    //	|			 |
    //	|____________|
    //(-1, -1)	1	(1, -1) 2
    // opengl世界坐标
    private val vertices = floatArrayOf(
        -0.5f, 0.5f,  // 0
        -0.5f, -0.5f, // 1
        0.5f, -0.5f,  // 2
        0.5f, 0.5f    // 3
    )

    private val color = floatArrayOf(
        0.0f, 0.0f, 0.0f, 1.0f
    )

    private val vertShader = """
        #version 300 es
        layout(location = 0) in vec4 aPosition;
        uniform mat4 uMatrix;

        void main() {
            gl_Position = uMatrix*aPosition;
            gl_PointSize = 10.0;
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
    private var uMatrixLocation: Int = 0
    private var uColorLocation: Int = 0

    private val matrix = FloatArray(16)

    init {
        Matrix.setIdentityM(matrix, 0)
    }

    /**
     * ！！！！！！！！！！！！！！！！！！！！！！
     * 这个方法的调用，必须在GL_THREAD,否则创建不了shader
     */
    override fun createShader() {
        super.createShader()
        glProgram = OpenGL.createProgram(vertShader, fragShader)
        uMatrixLocation = GLES30.glGetUniformLocation(glProgram, "uMatrix")
        uColorLocation = GLES30.glGetUniformLocation(glProgram, "uColor")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        // 指定大小
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glUseProgram(glProgram)

        GLES30.glUniform4fv(uColorLocation, 1, color, 0)
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(
            0,
            2,
            GLES30.GL_FLOAT,
            false,
            2 * 4,
            vertexBuffer
        )


        // (-1,1) 0		(1,1) 3
        //	 ____________
        //  |			 |
        //	|			 |
        //	|____________|
        //(-1, -1)	1	(1, -1) 2
        // opengl世界坐标

        // mode,指定要渲染的图元，
        // GL_POINTS, GL_LINES, GL_LINE_STRIP, GL_LINE_LOOP, GL_TRIANGLES,GL_TRIANGLES_STRIP,GL_TRIANGLES_FAN
        // first,指定启用的顶点数组中起始顶点的索引
        // count,指定要绘制的顶点数量,如果每个顶点xy,那么就是vertices.size/2
        // 如果每个顶点是xyz,那么就是vertices.size/3

        // 画点 0 1 2 3
        // GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 4);

        // 画线 01 23
        // GLES30.glDrawArrays(GLES30.GL_LINES, 0, 4);

        // 画线 01 12 23
        // GLES30.glDrawArrays(GLES30.GL_LINE_STRIP, 0, 4);

        // 画线 01 12 23 31
        // GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, 4);

        // 画三角形 012
        // GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 4);

        // 画三角形 根据奇偶规则 012 132 ? 不确定?
        //GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        // 画三角形 012 023 ? 不确定?
        // 数组共有vertices.size个元素，其中每个元素有xy两位，所以是vertices.size/2
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, vertices.size / 2)

        GLES30.glDisableVertexAttribArray(0)
    }
}