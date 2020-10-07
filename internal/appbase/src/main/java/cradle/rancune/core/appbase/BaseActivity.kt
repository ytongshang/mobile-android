package cradle.rancune.core.appbase

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Rancune@126.com 2020/10/6.
 */
abstract class BaseActivity : AppCompatActivity() {

    @Suppress("PropertyName")
    protected val TAG = this::class.java.simpleName

    abstract fun initView()

    abstract fun initData()

    open fun initToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initToolbar()
        initData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                this.finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }
}