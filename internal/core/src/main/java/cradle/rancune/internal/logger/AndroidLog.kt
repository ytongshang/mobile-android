package cradle.rancune.internal.logger

import android.util.Log

/**
 * Created by Rancune@126.com 2018/12/14.
 */
object AndroidLog : ILog {
    @Volatile
    private var logLevel = ILog.INFO

    override fun setLogLevel(level: Int) {
        var l = level
        if (level <= ILog.INFO) {
            l = ILog.INFO
        }
        if (level >= ILog.ERROR) {
            l = ILog.ERROR
        }
        logLevel = l
    }

    override fun getDefaultTag(): String {
        return "Router"
    }

    override fun v(tag: String?, msg: String?) {
        if (logLevel <= ILog.VERBOSE) {
            Log.v(getTag(tag), msg)
        }
    }

    override fun d(tag: String?, msg: String?) {
        if (logLevel <= ILog.DEBUG) {
            Log.d(getTag(tag), msg)
        }
    }

    override fun i(tag: String?, msg: String?) {
        if (logLevel <= ILog.INFO) {
            Log.i(getTag(tag), msg)
        }
    }

    override fun w(tag: String?, msg: String?) {
        if (logLevel <= ILog.WARN) {
            Log.w(getTag(tag), msg)
        }
    }

    override fun e(tag: String?, msg: String?) {
        if (logLevel <= ILog.ERROR) {
            Log.e(getTag(tag), msg)
        }
    }

    override fun e(tag: String?, msg: String?, tr: Throwable?) {
        if (logLevel <= ILog.ERROR) {
            Log.e(getTag(tag), msg, tr)
        }
    }

    private fun getTag(tag: String?): String {
        return if (tag.isNullOrEmpty()) defaultTag else tag
    }
}