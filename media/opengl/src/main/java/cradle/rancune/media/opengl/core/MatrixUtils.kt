package cradle.rancune.media.opengl.core

import android.opengl.Matrix

/**
 * Created by Rancune@126.com 2020/9/9.
 */
object MatrixUtils {
    enum class ScaleTye {
        FIT_XY, FIT_START, FIT_END, CENTER_CROP, CENTER_INSIDE
    }

    fun getMatrix(
        matrix: FloatArray,
        type: ScaleTye,
        imgWidth: Int,
        imgHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ) {
        val projection = FloatArray(16)
        val camera = FloatArray(16)
        if (type == ScaleTye.FIT_XY) {
            Matrix.orthoM(projection, 0, -1f, 1f, -1f, 1f, 1f, 3f)
            Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
        }
        val sWhView = viewWidth.toFloat() / viewHeight
        val sWhImg = imgWidth.toFloat() / imgHeight
        if (sWhImg > sWhView) {
            when (type) {
                ScaleTye.CENTER_CROP -> Matrix.orthoM(
                    projection,
                    0,
                    -sWhView / sWhImg,
                    sWhView / sWhImg,
                    -1f,
                    1f,
                    1f,
                    3f
                )
                ScaleTye.CENTER_INSIDE -> Matrix.orthoM(
                    projection,
                    0,
                    -1f,
                    1f,
                    -sWhImg / sWhView,
                    sWhImg / sWhView,
                    1f,
                    3f
                )
                ScaleTye.FIT_START -> Matrix.orthoM(
                    projection,
                    0,
                    -1f,
                    1f,
                    1 - 2 * sWhImg / sWhView,
                    1f,
                    1f,
                    3f
                )
                ScaleTye.FIT_END -> Matrix.orthoM(
                    projection,
                    0,
                    -1f,
                    1f,
                    -1f,
                    2 * sWhImg / sWhView - 1,
                    1f,
                    3f
                )
                else -> {

                }
            }
        } else {
            when (type) {
                ScaleTye.CENTER_CROP -> Matrix.orthoM(
                    projection,
                    0,
                    -1f,
                    1f,
                    -sWhImg / sWhView,
                    sWhImg / sWhView,
                    1f,
                    3f
                )
                ScaleTye.CENTER_INSIDE -> Matrix.orthoM(
                    projection,
                    0,
                    -sWhView / sWhImg,
                    sWhView / sWhImg,
                    -1f,
                    1f,
                    1f,
                    3f
                )
                ScaleTye.FIT_START -> Matrix.orthoM(
                    projection,
                    0,
                    -1f,
                    2 * sWhView / sWhImg - 1,
                    -1f,
                    1f,
                    1f,
                    3f
                )
                ScaleTye.FIT_END -> Matrix.orthoM(
                    projection,
                    0,
                    1 - 2 * sWhView / sWhImg,
                    1f,
                    -1f,
                    1f,
                    1f,
                    3f
                )
                else -> {

                }
            }
        }
        Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
    }

    fun flip(
        matrix: FloatArray,
        flipX: Boolean,
        flipY: Boolean,
        flipZ: Boolean
    ) {
        if (flipX || flipY || flipZ) {
            Matrix.scaleM(
                matrix, 0,
                if (flipX) (-1).toFloat() else 1.toFloat(),
                if (flipY) (-1).toFloat() else 1.toFloat(),
                if (flipZ) (-1).toFloat() else 1.toFloat()
            )
        }
    }


}
