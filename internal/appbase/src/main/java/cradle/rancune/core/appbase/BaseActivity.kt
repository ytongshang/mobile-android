package cradle.rancune.core.appbase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Rancune@126.com 2020/10/6.
 */
abstract class BaseActivity : AppCompatActivity() {

    @Suppress("PropertyName")
    protected val TAG = this::class.java.simpleName

    abstract fun initView()

    abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }
}