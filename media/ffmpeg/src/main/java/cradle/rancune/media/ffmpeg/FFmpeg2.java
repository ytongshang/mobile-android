package cradle.rancune.media.ffmpeg;

/**
 * Created by Rancune@126.com 2020/3/30.
 */
public class FFmpeg2 {
    static  {
        System.loadLibrary("native-lib");
    }
    public native String getCoder();
}
