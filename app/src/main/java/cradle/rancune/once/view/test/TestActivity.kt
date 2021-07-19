package cradle.rancune.once.view.test

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import cradle.rancune.core.appbase.BaseActivity

/**
 * Created by Rancune@126.com 4/5/21.
 */
class TestActivity: BaseActivity() {
  override fun initView() {
  }

  override fun initData() {
  }

  override fun onResume() {
    super.onResume()
    lifecycle.addObserver(object : LifecycleEventObserver {
      override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d("Rancune", "onResume: event:$event")
      }
    })
  }

  override fun onStop() {
    super.onStop()
    lifecycle.addObserver(object : LifecycleEventObserver {
      override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d("Rancune", "onStop: event:$event")
      }
    })
  }
}