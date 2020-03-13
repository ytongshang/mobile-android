package cradle.rancune.once.view

import android.os.Bundle
import cradle.rancune.once.R
import cradle.rancune.once.view.base.BaseActivity

/**
 * Created by Rancune@126.com 2020/3/13.
 */
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.once_activity_main)
    }
}