package cradle.rancune.apm.memory

import android.content.ComponentCallbacks2
import android.content.res.Configuration

/**
 * Created by Rancune@126.com 3/9/21.
 */
class MemoryMonitor : ComponentCallbacks2 {

  override fun onConfigurationChanged(newConfig: Configuration) {
  }

  override fun onLowMemory() {
  }

  override fun onTrimMemory(level: Int) {
  }
}