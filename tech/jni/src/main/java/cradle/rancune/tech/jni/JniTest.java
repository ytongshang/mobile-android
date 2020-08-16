package cradle.rancune.tech.jni;

/**
 * Created by Rancune@126.com 2020/5/12.
 */
public class JniTest {
    static {
        System.loadLibrary("jnitest");
    }

    public static native int plus(int a, int b);

    public static native int minus(int a, int b);

    public static native String getNativeString(String string);

    public native void print(String string);

}
