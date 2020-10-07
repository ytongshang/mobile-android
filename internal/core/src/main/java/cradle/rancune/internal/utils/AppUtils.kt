package cradle.rancune.internal.utils

import android.app.Application

/**
 * Created by Rancune@126.com 2020/10/7.
 */
object AppUtils {
    private var context: Application? = null

    fun initialize(app: Application) {
        this.context = app
    }

    val application: Application
        get() = context!!
}