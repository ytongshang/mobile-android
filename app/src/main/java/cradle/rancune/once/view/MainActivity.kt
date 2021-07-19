package cradle.rancune.once.view

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.media.opengl.ui.OpenglMainactivity
import cradle.rancune.once.R
import cradle.rancune.once.view.decode.MediaCodecVideoPlayActivity
import cradle.rancune.once.view.jni.JniTestActivity
import cradle.rancune.once.view.player.AACPlayerActivity
import cradle.rancune.once.view.player.PCMPlayerActivity
import cradle.rancune.once.view.record.AudioRecordActivity
import cradle.rancune.once.view.test.TestActivity
import cradle.rancune.widget.recyclerview.CommonRecyclerViewAdapter
import cradle.rancune.widget.recyclerview.OnItemClickListener
import kotlinx.android.synthetic.main.once_activity_main.*
import java.util.*

/**
 * Created by Rancune@126.com 2020/3/13.
 */
class MainActivity : BaseActivity() {

  private lateinit var adapter: CommonRecyclerViewAdapter<Page>
  private val pages: MutableList<Page> = ArrayList()

  override fun initView() {
    setContentView(R.layout.once_activity_main)
    recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    adapter = object : CommonRecyclerViewAdapter<Page>(
        pages,
        cradle.rancune.media.opengl.R.layout.opengl_reycle_item_main_page,
        object : OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                val page = pages.getOrNull(position)
                page?.target?.apply {
                    val i = Intent(v.context, this)
                    startActivity(i)
                }
                page?.runnable?.apply {
                    this.run()
                }
            }
        }) {
      override fun bind(holder: ViewHolder, item: Page) {
        holder.setText(cradle.rancune.media.opengl.R.id.tvPageName, item.title)
      }
    }
    recyclerView.adapter = adapter
  }

  override fun initToolbar() {
  }

  override fun initData() {
    val audioRecord = Page()
    audioRecord.title = R.string.once_activity_audio_record
    audioRecord.target = AudioRecordActivity::class.java
    pages.add(audioRecord)

    val pcmPlayer = Page()
    pcmPlayer.title = R.string.once_activity_pcm_player
    pcmPlayer.target = PCMPlayerActivity::class.java
    pages.add(pcmPlayer)

    val aacPlayer = Page()
    aacPlayer.title = R.string.once_activity_aac_player
    aacPlayer.target = AACPlayerActivity::class.java
    pages.add(aacPlayer)


    val codecvideo = Page()
    codecvideo.title = R.string.once_activity_video_player
    codecvideo.target = MediaCodecVideoPlayActivity::class.java
    pages.add(codecvideo)

    val jniTest = Page()
    jniTest.title = R.string.once_activity_jni_test
    jniTest.target = JniTestActivity::class.java
    pages.add(jniTest)

    val opengl = Page()
    opengl.title = R.string.once_activity_opengl
    opengl.target = OpenglMainactivity::class.java
    pages.add(opengl)

    val test = Page()
    test.title = R.string.once_activity_test
    test.target = TestActivity::class.java
    pages.add(test)

    adapter.notifyDataSetChanged()
  }

  private class Page {
    @StringRes
    var title = 0
    var target: Class<out Activity?>? = null
    var runnable: Runnable? = null
  }
}