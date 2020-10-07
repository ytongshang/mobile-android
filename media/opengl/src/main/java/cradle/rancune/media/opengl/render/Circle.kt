package cradle.rancune.media.opengl.render

import android.opengl.GLES30
import android.opengl.Matrix
import cradle.rancune.media.opengl.SimpleGlDrawer
import cradle.rancune.media.opengl.core.OpenGL
import java.nio.FloatBuffer
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by Rancune@126.com 2020/10/6.
 */
class Circle : SimpleGlDrawer {

    companion object {
        private val vertShader = """
        #version 300 es
        layout(location = 0) in vec4 aPosition;
        uniform mat4 uMatrix;

        void main() {
            gl_Position = uMatrix*aPosition;
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
    }

    private val matrix = FloatArray(16)

    init {
        Matrix.setIdentityM(matrix, 0)
    }

    private val vertices = createPositions(0.5f, 256)
    private val vertexBuffer: FloatBuffer = OpenGL.createFloatBuffer(vertices)

    // 黑色
    private val color: FloatArray = floatArrayOf(
        0.0f, 0.0f, 0.0f, 1.0f
    )
    private var glProgram: Int = 0
    private var uMatrixLocation: Int = 0
    private var uColorLocation: Int = 0

    /**
     * ！！！！！！！！！！！！！！！！！！！！！！
     * 这个方法的调用，必须在GL_THREAD,否则创建不了shader
     */
    override fun createShader() {
        glProgram = OpenGL.createProgram(vertShader, fragShader)
        uMatrixLocation = GLES30.glGetUniformLocation(glProgram, "uMatrix")
        uColorLocation = GLES30.glGetUniformLocation(glProgram, "uColor")
    }

    override fun onSurfaceCreated() {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        // 指定大小
        GLES30.glViewport(0, 0, width, height)
        // 虽然每个顶点坐标都是0.5，但是屏幕的宽*0.5 与高*0.5 不一样，这样以较小的为1，较长为的其比例
        val ratio = if (width > height) width / height.toFloat() else height.toFloat() / width
        if (width > height) {
            Matrix.orthoM(matrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
        } else {
            Matrix.orthoM(matrix, 0, -1f, 1f, -ratio, ratio, -1f, 1f)
        }
    }

    override fun onDrawFrame() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glUseProgram(glProgram)
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        GLES30.glUniform4fv(uColorLocation, 1, color, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, vertexBuffer)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, vertices.size / 2)
        GLES30.glDisableVertexAttribArray(0)
    }

    override fun destroy() {
        if (glProgram > 0) {
            GLES30.glDeleteProgram(glProgram)
            glProgram = 0
        }
    }

    private fun createPositions(radius: Float, n: Int): FloatArray {
        val list: MutableList<Float> = ArrayList()
        // 一系列顶点中的第一个点为中心点，其他顶点为边缘点，绘制一系列组成扇形的相邻三角形
        // 中心点在(0, 0)其它的顺序组成扇形，最终组成圆形
        list.add(0.0f)
        list.add(0.0f)
        val ang = (360 / n).toFloat()
        var i = 0
        // 角度转弧度 π/180×角度；弧度du变角度 180/π×弧度
        while (i < 360 + ang) {
            list.add((radius * cos(i * Math.PI / 180)).toFloat())
            list.add((radius * sin(i * Math.PI / 180)).toFloat())
            i += ang.toInt()
        }
        val f = FloatArray(list.size)
        for (i in f.indices) {
            f[i] = list[i]
        }
        return f
    }
}