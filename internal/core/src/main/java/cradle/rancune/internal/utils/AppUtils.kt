package cradle.rancune.internal.utils

import android.app.Application
import cradle.rancune.internal.logger.ILog
import cradle.rancune.internal.logger.L

/**
 * Created by Rancune@126.com 2020/10/7.
 */
object AppUtils {
    private var context: Application? = null

    fun initialize(app: Application) {
        this.context = app
        L.setLogLevel(ILog.VERBOSE)
    }

    val application: Application
        get() = context!!
}