package cradle.rancune.internal.logger

/**
 * Created by Rancune@126.com 2020/9/9.
 */
object L : ILog {
    private val log = AndroidLog

    override fun setLogLevel(level: Int) {
        log.setLogLevel(level)
    }

    override fun getDefaultTag(): String {
        return "Rancune"
    }

    override fun i(tag: String?, msg: String?) {
        log.i(tag, msg)
    }

    override fun d(tag: String?, msg: String?) {
        log.d(tag, msg)
    }

    override fun v(tag: String?, msg: String?) {
        log.i(tag, msg)
    }

    override fun w(tag: String?, msg: String?) {
        log.i(tag, msg)
    }

    override fun e(tag: String?, msg: String?) {
        log.i(tag, msg)
    }

    override fun e(tag: String?, msg: String?, tr: Throwable?) {
        log.i(tag, msg)
    }
}