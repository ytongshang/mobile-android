package cradle.rancune.media

/**
 * Created by Rancune@126.com 2020/3/22.
 */
interface OnErrorListener {
    fun onError(what: Int, extra: Any? = null, throwable: Throwable? = null)
}