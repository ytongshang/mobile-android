package cradle.rancune.internal.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;


import java.lang.reflect.Field;

import cradle.rancune.internal.logger.AndroidLog;

@SuppressWarnings({"WeakerAccess", "unused"})
public class T {
    private static final String TAG = "T";

    private static Application sApplication;
    private static Toast toast = null;
    private static String oldMsg;
    private static long oldTime = 0;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private T() {
    }

    public static void setApplication(Application application) {
        sApplication = application;
    }

    public static void showShort(String message) {
        Context context = application();
        if (context != null && !TextUtils.isEmpty(message)) {
            showToast(context, message);
        }
    }

    public static void showShort(@StringRes int resId) {
        Context context = application();
        if (context != null) {
            String msg = context.getString(resId);
            showShort(msg);
        }
    }

    public static void showShort(@StringRes int resId, Object... args) {
        Context context = application();
        if (context != null) {
            String msg = context.getString(resId, args);
            showShort(msg);
        }
    }

    private static void showToast(final Context context, final String s) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            toast(context, s);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    toast(context, s);
                }
            });
        }
    }

    private static void toast(@NonNull Context context, @NonNull String msg) {
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), null, Toast.LENGTH_SHORT);
            proxy(toast);
            // 这种方式，在小米上不显示应用名字
            toast.setText(msg);
            toast.show();
            oldMsg = msg;
            oldTime = System.currentTimeMillis();
        } else {
            long current = System.currentTimeMillis();
            if (msg.equals(oldMsg)) {
                if (current - oldTime > 2000) {
                    toast.show();
                    oldTime = current;
                }
            } else {
                oldMsg = msg;
                toast.setText(msg);
                toast.show();
                oldTime = current;
            }
        }
    }

    private static class HandlerProxy extends Handler {
        private Handler handler;

        HandlerProxy(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                handler.handleMessage(msg);
            } catch (Exception e) {
                AndroidLog.INSTANCE.e(TAG, "", e);
            }
        }
    }

    private static void proxy(Toast toast) {
        if (toast == null) {
            return;
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field fieldTN = Toast.class.getDeclaredField("mTN");
                fieldTN.setAccessible(true);
                Object objTN = fieldTN.get(toast);
                if (objTN != null) {
                    Class classTN = objTN.getClass();
                    Field fieldHandler = classTN.getDeclaredField("mHandler");
                    fieldHandler.setAccessible(true);
                    fieldHandler.set(objTN, new HandlerProxy((Handler) fieldHandler.get(objTN)));
                }
            } catch (Throwable e) {
                AndroidLog.INSTANCE.e(TAG, "", e);
            }
        }
    }

    @SuppressLint("PrivateApi")
    private static Application application() {
        if (sApplication != null) {
            return sApplication;
        }
        synchronized (T.class) {
            if (sApplication == null) {
                try {
                    sApplication = (Application) Class.forName("android.app.ActivityThread")
                            .getMethod("currentApplication").invoke(null, (Object[]) null);
                } catch (Exception ignored) {
                }
            }
        }
        return sApplication;
    }
}
