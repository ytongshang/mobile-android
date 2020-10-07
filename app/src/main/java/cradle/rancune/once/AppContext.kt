package cradle.rancune.once

import android.app.Application
import android.os.Handler
import android.os.Looper
import cradle.rancune.internal.utils.AppUtils

class AppContext : Application(), Runnable {
    val hander = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        AppUtils.initialize(this)
    }

    override fun run() {
    }
}