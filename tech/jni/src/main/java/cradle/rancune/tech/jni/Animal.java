package cradle.rancune.tech.jni;

import androidx.annotation.Keep;

import cradle.rancune.internal.logger.AndroidLog;

/**
 * Created by Rancune@126.com 2020/10/4.
 */
@Keep
public class Animal {
    private static int num = 0;

    private String name;

    public Animal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Animal setName(String name) {
        this.name = name;
        return this;
    }

    public static int getNum() {
        return num;
    }

    public static void setNum(int num) {
        Animal.num = num;
    }

    public void method1() {
        AndroidLog.INSTANCE.d(JniTest.TAG, "method1");
    }
}
