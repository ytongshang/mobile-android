package cradle.rancune.media.opengl.render

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import android.opengl.Matrix
import cradle.rancune.media.opengl.SimpleGlDrawer
import cradle.rancune.media.opengl.core.MatrixUtils
import cradle.rancune.media.opengl.core.OpenGL

/**
 * Created by Rancune@126.com 2020/10/7.
 */
class Saturation : SimpleGlDrawer {
    companion object {
        private val vertShader = """
        #version 300 es
        layout(location = 0) in vec4 aPosition;
        layout(location = 1) in vec2 aTextureCoord;
        uniform mat4 uMvpMatrix;
        uniform mat4 uTextureCoordMatrix;
        out vec2 vTextureCoord;

        void main() {
            gl_Position = uMvpMatrix*aPosition;
            vTextureCoord = (uTextureCoordMatrix*vec4(aTextureCoord, 0 ,1)).xy;
        }
    """.trimIndent()

        /**
         * https://blog.csdn.net/lb377463323/article/details/77047221
         * OpenGL ES 2.0的gl_FragColor和gl_FragData在3.0中取消掉了，需要自己定义out变量作为片段着色器的输出颜色
         * OpenGL ES 3.0的shader中没有texture2D和texture3D等了，全部使用texture替换
         */
        private val fragShader = """
        #version 300 es
        precision mediump float;
        in vec2 vTextureCoord;
        uniform sampler2D uTexture;
        uniform float uSaturation;
        out vec4 fragColor;
         
        const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

        void main () {
            vec4 textureColor = texture(uTexture, vTextureCoord);
            float luminance = dot(textureColor.rgb, luminanceWeighting);
            vec3 greyScaleColor = vec3(luminance);
            fragColor = vec4(mix(greyScaleColor, textureColor.rgb, uSaturation), textureColor.w);
        }
    """.trimIndent()
    }

    // (-1,1) 0		(1,1) 3
    //	 ____________
    //  |			 |
    //	|			 |
    //	|____________|
    //(-1, -1)	1	(1, -1) 2
    // opengl世界坐标
    private val worldVertices = floatArrayOf(
        -1.0f, 1.0f,    // 0
        -1.0f, -1.0f,   // 1
        1.0f, -1.0f,    // 2
        1.0f, 1.0f     // 3
    )

    // (0,1) 0		(1,1) 3
    //	 ____________
    //  |			 |
    //	|			 |
    //	|____________|
    //(0, 0) 1	(1, 0) 2
    // OpenGL默认纹理坐标
    private val textureCoords = floatArrayOf(
        0.0f, 1.0f,   // 0
        0.0f, 0.0f,   // 1
        1.0f, 0.0f,   // 2
        1.0f, 1.0f    // 3
    )

    // (0,0) 0		(1,0) 3
    //	 ____________
    //  |			 |
    //	|			 |
    //	|____________|
    //(0, 1) 1	(1, 1) 2
    // Android纹理坐标，这个只有在2D贴图显示到android屏幕的坐标上才是正确的，其他还是默认OpenGL的坐标系
    private val androidTextureCoords = floatArrayOf(
        0.0f, 0.0f,   // 0
        0.0f, 1.0f,   // 1
        1.0f, 1.0f,   // 2
        1.0f, 0.0f    // 3
    )
    private val vertexBuffer = OpenGL.createFloatBuffer(worldVertices)
    private val textureCoordBuffer = OpenGL.createFloatBuffer(androidTextureCoords)

    private var glProgram: Int = -1
    private var uMvpMatrixLocation: Int = -1
    private var uTextureCoordMatrixLocation: Int = -1
    private var textureLocation: Int = -1
    private var uSaturationLocation: Int = -1

    private val mvpMatrix = FloatArray(16)
    private val textureCoordMatrix = FloatArray(16)

    init {
        Matrix.setIdentityM(mvpMatrix, 0)
        Matrix.setIdentityM(textureCoordMatrix, 0)
    }

    private var bitmap: Bitmap? = null
    private var textureId: Int = 0
    private var saturation: Float = 0.0f

    private var viewWidth = 0
    private var viewHeight = 0

    override fun createShader() {
        glProgram = OpenGL.createProgram(vertShader, fragShader)
        uMvpMatrixLocation = GLES30.glGetUniformLocation(glProgram, "uMvpMatrix")
        uTextureCoordMatrixLocation = GLES30.glGetUniformLocation(glProgram, "uTextureCoordMatrix")
        textureLocation = GLES30.glGetUniformLocation(glProgram, "uTexture")
        uSaturationLocation = GLES30.glGetUniformLocation(glProgram, "uSaturation")
        textureId = OpenGL.createTexture()
    }

    override fun onSurfaceCreated() {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1f)
        // 开启2D纹理
        GLES30.glEnable(GLES30.GL_TEXTURE_2D)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.viewWidth = width
        this.viewHeight = height
        GLES30.glViewport(0, 0, width, height)
        bitmap?.let {
            MatrixUtils.getMatrix(
                mvpMatrix, MatrixUtils.ScaleTye.CENTER_INSIDE,
                it.width, it.height, width, height
            )
        }

    }

    override fun onDrawFrame() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLES30.glUseProgram(glProgram)
        // 设置饱和度
        GLES20.glUniform1f(uSaturationLocation, saturation)
        // mvp矩阵
        GLES30.glUniformMatrix4fv(uMvpMatrixLocation, 1, false, mvpMatrix, 0)
        // 纹理矩阵
        GLES30.glUniformMatrix4fv(uTextureCoordMatrixLocation, 1, false, textureCoordMatrix, 0)
        // 启用纹理0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        // 设置bitmap生成纹理
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        // 将我们生成的textureId与GLES30.GL_TEXTURE0关联到一起
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        // 因为上面是GLES30.GL_TEXTURE0，所以这里是0
        GLES30.glUniform1i(textureLocation, 0)

        // 顶点坐标
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(
            0,
            2,
            GLES30.GL_FLOAT,
            false,
            2 * 4,
            vertexBuffer
        )

        // 纹理坐标
        // 纹理坐标是与顶点坐标一一对应的
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(
            1,
            2,
            GLES30.GL_FLOAT,
            false,
            2 * 4,
            textureCoordBuffer
        )
        // [0,1,2],[0,2,3]
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, worldVertices.size / 2)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    fun setSaturation(saturation: Float) {
        this.saturation = saturation
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        bitmap.let {
            MatrixUtils.getMatrix(
                mvpMatrix, MatrixUtils.ScaleTye.CENTER_INSIDE,
                it.width, it.height, viewWidth, viewHeight
            )
        }
    }

    override fun destroy() {
        if (textureId > 0) {
            GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = 0
        }
        if (glProgram > 0) {
            GLES30.glDeleteProgram(glProgram)
            glProgram = 0
        }
    }
}