package cradle.rancune.once.view.jni

import cradle.rancune.core.appbase.BaseActivity
import cradle.rancune.once.R
import cradle.rancune.tech.jni.JniTest

/**
 * Created by Rancune@126.com 2020/3/30.
 */
class JniTestActivity : BaseActivity() {

    private val jniTest = JniTest()

    override fun initView() {
        setContentView(R.layout.once_activity_jni_test)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.setTitle(R.string.once_activity_jni_test)
    }

    override fun initData() {
        jniTest.test()
    }
}