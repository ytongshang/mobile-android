@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package cradle.rancune.media.opengl.core

import android.opengl.GLES11Ext
import android.opengl.GLES30
import cradle.rancune.internal.logger.L
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by Rancune@126.com 2020/9/9.
 */
object OpenGL {

    private const val TAG = "OpenGL"

    fun createProgram(vertexCode: String?, fragmentCode: String?): Int {
        if (vertexCode.isNullOrEmpty() || fragmentCode.isNullOrEmpty()) {
            return -1
        }
        val vertex = compileShader(
            vertexCode,
            GLES30.GL_VERTEX_SHADER
        )
        val fragment = compileShader(
            fragmentCode,
            GLES30.GL_FRAGMENT_SHADER
        )
        if (vertex == -1 || fragment == -1) {
            return -1
        }
        return linkProgram(
            vertex,
            fragment
        )
    }

    private fun compileShader(code: String?, type: Int): Int {
        if (code.isNullOrEmpty()) {
            return -1
        }
        val shader = GLES30.glCreateShader(type)
        if (shader == 0) {
            L.e(TAG, "compileShader failed, code: $code")
            return -1
        }
        GLES30.glShaderSource(shader, code)
        GLES30.glCompileShader(shader)
        val status = IntArray(1)
        // GLES30.GL_COMPILE_STATUS
        // GLES30.GL_DELETE_STATUS
        // GLES30.GL_INFO_LOG_LENGTH
        // GLES30.GL_SHADER_SOURCE_LENGTH
        // GLES30.GL_SHADER_TYPE
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] != GLES30.GL_TRUE) {
            val length = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_INFO_LOG_LENGTH, length, 0)
            val log = if (length[0] > 0) {
                "Can not compile shader: ${GLES30.glGetShaderInfoLog(shader)}"
            } else {
                "Can not compile shader: Unknown error"
            }
            L.e(TAG, log)
            GLES30.glDeleteShader(shader)
            return -1
        }
        return shader
    }

    private fun linkProgram(vertexShader: Int, fragmentShader: Int): Int {
        if (vertexShader <= 0 && fragmentShader <= 0) {
            return -1
        }
        val program = GLES30.glCreateProgram()
        if (program == 0) {
            L.e(TAG, "Can not create program")
            return -1
        }
        if (vertexShader > 0) {
            GLES30.glAttachShader(program, vertexShader)
        }  vz
        if (fragmentShader > 0) {
            GLES30.glAttachShader(program, fragmentShader)
        }
        GLES30.glLinkProgram(program)
        val status = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0)
        if (status[0] != GLES30.GL_TRUE) {
            val length = IntArray(1)
            GLES30.glGetProgramiv(program, GLES30.GL_INFO_LOG_LENGTH, length, 0)
            val log = if (length[0] > 0) {
                "Can not link program: ${GLES30.glGetProgramInfoLog(program)}"
            } else {
                "Can not link program: Unknown error"
            }
            L.e(TAG, log)
            GLES30.glDeleteProgram(program)
            return -1
        }
        return program
    }

    fun checkError(op: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            val msg = op + " glError :" + errorToString(error)
            L.e(TAG, msg)
            throw RuntimeException(msg)
        }
    }

    private fun errorToString(error: Int): String {
        return when (error) {
            GLES30.GL_NO_ERROR -> {
                "GLES30.GL_NO_ERROR"
            }
            GLES30.GL_INVALID_ENUM -> {
                "GLES30.GL_INVALID_ENUM"
            }
            GLES30.GL_INVALID_VALUE -> {
                "GLES30.GL_INVALID_VALUE"
            }
            GLES30.GL_INVALID_OPERATION -> {
                "GLES30.GL_INVALID_OPERATION"
            }
            GLES30.GL_OUT_OF_MEMORY -> {
                "GLES30.GL_OUT_OF_MEMORY"
            }
            else -> {
                "Unknown Error"
            }
        }
    }

    fun createFloatBuffer(vertices: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(vertices)
        fb.position(0)
        return fb
    }

    fun createShortBuffer(indices: ShortArray): ShortBuffer {
        val bb = ByteBuffer.allocateDirect(indices.size * 2)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asShortBuffer()
        fb.put(indices)
        fb.position(0)
        return fb
    }

    fun createTexture(size: Int): IntArray {
        check(size > 0) {
            "size should greater than 0"
        }
        val textureObjectids = IntArray(size)
        GLES30.glGenTextures(size, textureObjectids, 0)
        checkError("glGenTextures")
        for (i in 0 until size) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureObjectids[i])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_NEAREST.toFloat()
            )
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR.toFloat()
            )
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S,
                GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T,
                GLES30.GL_CLAMP_TO_EDGE.toFloat()
            )
        }
        return textureObjectids
    }

    fun createTexture(): Int {
        return createTexture(1)[0]
    }

    fun createOESTexture(): Int {
        val texture = IntArray(1)
        GLES30.glGenTextures(1, texture, 0)
        checkError("glGenTextures")
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE
        )
        return texture[0]
    }
}

