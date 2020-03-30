package cradle.rancune.once.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cradle.rancune.once.R
import cradle.rancune.once.view.base.BaseActivity
import cradle.rancune.once.view.decode.MediaCodecVideoPlayActivity
import cradle.rancune.once.view.jni.JniTestActivity
import cradle.rancune.once.view.player.PCMPlayerActivity
import cradle.rancune.once.view.record.AudioRecordActivity
import kotlinx.android.synthetic.main.once_activity_main.*
import java.util.*

/**
 * Created by Rancune@126.com 2020/3/13.
 */
class MainActivity : BaseActivity() {


    private var adapter: Adapter? = null
    private val pages: MutableList<Page> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.once_activity_main)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = Adapter()
        recyclerView.adapter = adapter

        val audioRecord = Page()
        audioRecord.title = R.string.once_activity_audio_record
        audioRecord.target = AudioRecordActivity::class.java
        pages.add(audioRecord)

        val pcmPlayer = Page()
        pcmPlayer.title = R.string.once_activity_pcm_player
        pcmPlayer.target = PCMPlayerActivity::class.java
        pages.add(pcmPlayer)

        val codecvideo = Page()
        codecvideo.title = R.string.once_activity_video_player
        codecvideo.target = MediaCodecVideoPlayActivity::class.java
        pages.add(codecvideo)

        val jniTest = Page()
        jniTest.title = R.string.once_activity_jni_test
        jniTest.target = JniTestActivity::class.java
        pages.add(jniTest)

        adapter?.notifyDataSetChanged()
    }

    private inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
            val page: Page = pages[position]
            holder.bindView(page)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.once_item_main_page, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return pages.size
        }
    }

    private class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var page: Page? = null
        var textView: TextView = itemView.findViewById(R.id.tv_page_name)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindView(page: Page) {
            textView.setText(page.title)
            this.page = page
        }

        override fun onClick(v: View) {
            page?.let {
                val intent = Intent(v.context, it.target)
                v.context.startActivity(intent)
            }
        }
    }

    private class Page {
        @StringRes
        var title = 0
        var target: Class<out Activity?>? = null
    }
}