package cradle.rancune.once

import android.app.Application
import android.os.Handler
import android.os.Looper

class AppContext : Application(), Runnable {
    val hander = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
    }

    override fun run() {
    }
}