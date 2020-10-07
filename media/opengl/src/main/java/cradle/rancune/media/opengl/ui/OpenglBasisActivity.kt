package cradle.rancune.media.opengl.ui

import android.content.Context
import android.content.Intent
import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.media.opengl.R
import cradle.rancune.media.opengl.RenderWrapper
import cradle.rancune.media.opengl.SimpleGlRender
import cradle.rancune.media.opengl.render.*
import kotlinx.android.synthetic.main.opengl_activity_opengl.*

/**
 * Created by Rancune@126.com 2020/10/6.
 */
class OpenglBasisActivity : BaseActivity() {

    companion object {

        private const val TYPE_COLORTRIANGLE = 1
        private const val TYPE_SHAPE = 2
        private const val TYPE_CIRCLE = 3
        private const val TYPE_DRAWELEMENTS = 4
        private const val TYPE_SAMPLE2D = 5

        fun intentOf(context: Context, type: Int): Intent? {
            val intent = Intent(context, OpenglBasisActivity::class.java)
            intent.putExtra("rendererType", type)
            return intent
        }

        private fun getRenderClass(type: Int): Class<out SimpleGlRender> {
            when (type) {
                TYPE_COLORTRIANGLE -> {
                    return ColorTriangle::class.java
                }
                TYPE_SHAPE -> {
                    return Shape::class.java
                }
                TYPE_CIRCLE -> {
                    return Circle::class.java
                }
                TYPE_DRAWELEMENTS -> {
                    return DrawElements::class.java
                }
                TYPE_SAMPLE2D -> {
                    return Sample2D::class.java
                }
                else -> {
                    return ColorTriangle::class.java
                }
            }
        }
    }

    private lateinit var render: RenderWrapper
    private var renderType: Int = -1

    override fun initView() {
        setContentView(R.layout.opengl_activity_opengl)
    }

    override fun initToolbar() {
        super.initToolbar()
        renderType = intent.getIntExtra("rendererType", TYPE_COLORTRIANGLE)
        val identifier = "opengl_activity_opengl$renderType"
        val strRes = resources.getIdentifier(identifier, "string", packageName)
        if (strRes > 0) {
            supportActionBar?.setTitle(strRes)
        }
    }

    override fun initData() {
        glSurfaceView.setEGLContextClientVersion(3)
        render = RenderWrapper(getRenderClass(renderType))
        glSurfaceView.setRenderer(render)
    }

    override fun onStart() {
        super.onStart()
        glSurfaceView?.onResume()
    }

    override fun onStop() {
        glSurfaceView?.onPause()
        super.onStop()
    }
}