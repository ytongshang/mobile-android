package cradle.rancune.internal.core.logger;

import android.util.Log;

/**
 * Created by Rancune@126.com 2017/10/30.
 */

public interface ILog {

    int VERBOSE = Log.VERBOSE;

    /**
     * Priority constant for the println method; use Log.d.
     */
    int DEBUG = Log.DEBUG;

    /**
     * Priority constant for the println method; use Log.i.
     */
    int INFO = Log.INFO;

    /**
     * Priority constant for the println method; use Log.w.
     */
    int WARN = Log.WARN;

    /**
     * Priority constant for the println method; use Log.e.
     */
    int ERROR = Log.ERROR;

    void setLogLevel(int level);

    /**
     * default log tag
     */
    String getDefaultTag();

    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void v(String tag, String msg);


    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void d(String tag, String msg);


    /**
     * Send an {@link android.util.Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void i(String tag, String msg);


    /**
     * Send a {@link android.util.Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void w(String tag, String msg);


    /**
     * Send an {@link android.util.Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void e(String tag, String msg);

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    void e(String tag, String msg, Throwable tr);
}
