package cradle.rancune.internal.extension

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import cradle.rancune.internal.utils.AppUtils

/**
 * Created by Rancune@126.com 2020/10/7.
 */

val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

val Int.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

fun findDrawable(@DrawableRes id: Int): Drawable? =
    ContextCompat.getDrawable(AppUtils.application, id)

fun findString(@StringRes id: Int): String =
    AppUtils.application.getString(id)

fun findString(@StringRes id: Int, vararg any: Any?): String =
    AppUtils.application.getString(id, *any)

fun findColor(@ColorRes id: Int) =
    ContextCompat.getColor(AppUtils.application, id)

fun findDimen(@DimenRes id: Int): Int =
    AppUtils.application.resources.getDimensionPixelSize(id)

fun Context.screenSize(): Point {
    val manager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    manager.defaultDisplay.getSize(point)
    return point
}

fun String?.ifEmpty(@StringRes res: Int): String {
    if (this.isNullOrEmpty()) {
        return findString(res)
    }
    return this
}