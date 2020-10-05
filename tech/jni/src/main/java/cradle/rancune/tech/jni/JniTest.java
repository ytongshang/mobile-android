package cradle.rancune.tech.jni;

import androidx.annotation.Keep;

import java.util.Arrays;

import cradle.rancune.internal.logger.AndroidLog;

/**
 * Created by Rancune@126.com 2020/5/12.
 */
@Keep
public class JniTest {

    public static final String TAG = "JniTest";
    private static final Animal animal = new Animal("Cat");

    static {
        System.loadLibrary("jnitest");
        // 使用static代码块，在cacheFieldAndMethods里面缓存jclass,jmethodID
        // 这样就不用每次都查找
        cacheFieldAndMethods();
    }

    public static native void cacheFieldAndMethods();

    public static native String getNativeString(String string);

    public static native int intArraySum(int[] intArray, int num);

    public static native int[] getIntArray(int num);

    public static native int[][] getTwoDimensionalArray(int row, int column);

    public static native void printAnimalsName(Animal[] beans);

    public native void accessInstanceAndStaticField(Animal animal);

    public native void callInstanceAndStaticMethod(Animal animal);

    public native Animal invokeAnimalConstructor(String name);

    public native Animal allocAnimalConstructor(String name);

    public native void callSuperMethod(String name);

    public native Cat useCacheMethod(String name);

    public void test() {
        AndroidLog.INSTANCE.d(TAG, getNativeString("test"));
        int array[] = new int[10];
        for (int i = 0; i < 10; i++) {
            array[i] = i + 1;
        }
        // 55
        AndroidLog.INSTANCE.d(TAG, "intArraySum:" + intArraySum(array, 10));

        int[] nativeIntArray = getIntArray(10);
        AndroidLog.INSTANCE.d(TAG, Arrays.toString(nativeIntArray));

        int[][] twoArray = getTwoDimensionalArray(3, 4);
        AndroidLog.INSTANCE.d(TAG, "row:" + twoArray.length + ", column:" + twoArray[0].length);
        AndroidLog.INSTANCE.d(TAG, Arrays.toString(twoArray[0]));
        AndroidLog.INSTANCE.d(TAG, Arrays.toString(twoArray[1]));
        AndroidLog.INSTANCE.d(TAG, Arrays.toString(twoArray[2]));

        Animal[] animals = new Animal[3];
        animals[0] = new Animal("Animal0");
        animals[1] = new Animal("Animal1");
        animals[2] = new Animal("Animal2");
        printAnimalsName(animals);

        animal.setName("accessInstanceAndStaticField");
        Animal.setNum(0);
        accessInstanceAndStaticField(animal);
        AndroidLog.INSTANCE.d(TAG, animal.getName());
        AndroidLog.INSTANCE.d(TAG, String.valueOf(Animal.getNum()));

        animal.setName("accessInstanceAndStaticField");
        Animal.setNum(0);
        callInstanceAndStaticMethod(animal);
        AndroidLog.INSTANCE.d(TAG, animal.getName());
        AndroidLog.INSTANCE.d(TAG, String.valueOf(Animal.getNum()));

        Animal a = invokeAnimalConstructor("invokeAnimalConstructor test");
        AndroidLog.INSTANCE.d(TAG, a.getName());

        Animal b = allocAnimalConstructor("allocAnimalConstructor test");
        AndroidLog.INSTANCE.d(TAG, b.getName());

        callSuperMethod("callSuperMethod");

        Animal c = useCacheMethod("useCacheMethod");
        AndroidLog.INSTANCE.d(TAG, c.getName());
    }
}
