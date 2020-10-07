package cradle.rancune.media.opengl.ui

import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.media.opengl.R
import cradle.rancune.widget.recyclerview.CommonRecyclerViewAdapter
import cradle.rancune.widget.recyclerview.OnItemClickListener
import kotlinx.android.synthetic.main.opengl_activity_opengl_main.*
import java.util.*

/**
 * Created by Rancune@126.com 2020/10/7.
 */
class OpenglMainactivity : BaseActivity() {

    private val pages = ArrayList<Page>()
    private lateinit var adapter: CommonRecyclerViewAdapter<Page>

    override fun initView() {
        setContentView(R.layout.opengl_activity_opengl_main)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = object : CommonRecyclerViewAdapter<Page>(
            pages,
            R.layout.opengl_reycle_item_main_page,
            object : OnItemClickListener {
                override fun onItemClick(v: View, position: Int) {
                    pages.getOrNull(position)?.intent.apply {
                        startActivity(this)
                    }
                }
            }) {
            override fun bind(holder: ViewHolder, item: Page) {
                holder.setText(R.id.tvPageName, item.title)
            }
        }
        recyclerView.adapter = adapter
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.setTitle(R.string.opengl_activity_opengl)
    }

    override fun initData() {
        for (i in 1..5) {
            val page = Page()
            val identifier = "opengl_activity_opengl$i"
            page.title = resources.getIdentifier(identifier, "string", packageName)
            page.intent = OpenglBasisActivity.intentOf(this, i)
            pages.add(page)
        }

        val page = Page()
        page.title = R.string.opengl_activity_opengl7
        page.intent = Intent(this, OpenglSaturationActivity::class.java)
        pages.add(page)
//
//        val saturation = Page()
//        saturation.title = R.string.opengl_activity_opengl9
//        saturation.intent = Intent(mContext, OpenGLSaturationActivity::class.java)
//        pages.add(saturation)
//
//        val fbo = Page()
//        fbo.title = R.string.opengl_activity_opengl10
//        fbo.intent = Intent(mContext, OpenGLFboActivity::class.java)
//        pages.add(fbo)
//
//        val egl = Page()
//        egl.title = R.string.opengl_activity_opengl11
//        egl.intent = Intent(mContext, OpenGLEglActivity::class.java)
//        pages.add(egl)

        adapter.notifyDataSetChanged()
    }

    private class Page {
        @StringRes
        var title = 0
        var intent: Intent? = null
    }
}