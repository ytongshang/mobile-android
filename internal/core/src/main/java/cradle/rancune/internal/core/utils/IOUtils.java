package cradle.rancune.internal.core.utils;


import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;

import cradle.rancune.internal.core.logger.AndroidLog;


/**
 * Created by Rancune@126.com on 2016/7/29.
 */
@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public final class IOUtils {

    private static final String TAG = "IOUtils";

    private IOUtils() {
    }

    /**
     * @param closeables the Closeable object array to be closed, or null,in which case we do nothing
     */
    public static void closeQuietly(@Nullable Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            AndroidLog.INSTANCE.e(TAG, "IOUtils:IOException thrown while closing Closeable.", e);
        }
    }

    /**
     * @param in  inputstream
     * @param out outputstream
     * @throws IOException IOException
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        if (in == null || out == null) {
            return 0;
        }
        long count = 0L;
        int len;
        byte[] buffer = new byte[4096];
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
            count += len;
        }
        out.flush();
        return count;
    }

    /**
     * @param input inputstream
     * @return byte[]
     * @throws IOException IOException
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        if (input == null) {
            return new byte[0];
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * @param in      inputstream
     * @param charset charset, default UTF-8
     * @return the string from the inputstream, or empty string if the inputstring is null
     * @throws IOException IOException
     */
    public static String readString(InputStream in, String charset) throws IOException {
        if (in == null) {
            return "";
        }
        if (charset == null || charset.isEmpty()) {
            charset = "UTF-8";
        }
        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }
        Reader reader = new InputStreamReader(in, charset);
        StringBuilder builder = new StringBuilder();
        char[] buf = new char[1024];
        int len;
        while ((len = reader.read(buf)) != -1) {
            builder.append(buf, 0, len);
        }
        return builder.toString();
    }

    /**
     * @param out     outputstream
     * @param text    content
     * @param charset charset,default UTF-8
     * @throws IOException write a string to the outputstream
     */
    public static void writeString(OutputStream out, String text, String charset) throws IOException {
        if (out == null || text == null) {
            return;
        }
        if (charset == null || charset.isEmpty()) {
            charset = "UTF-8";
        }
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new StringReader(text));
            writer = new BufferedWriter(new OutputStreamWriter(out, charset));
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, len);
            }
            writer.flush();
        } finally {
            closeQuietly(writer);
            closeQuietly(reader);
        }
    }

    /**
     * @param file    the source file
     * @param charset charset,default UTF-8
     * @return a string from the file,  or empty string
     */
    public static String readFile(File file, String charset) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return "";
        }
        InputStream in = null;
        String str = "";
        try {
            in = new FileInputStream(file);
            str = readString(in, charset);
        } catch (IOException e) {
            AndroidLog.INSTANCE.e(TAG, "IOUtils:read string from file=" + file + " failed", e);
        } finally {
            closeQuietly(in);
        }
        return str;
    }

    /**
     * @param text    content
     * @param charset charset,default UTF-8
     * @param file    the destination file
     * @return whether suceess
     * write the string to the file
     */
    public static boolean writeFile(String text, String charset, File file) {
        if (text == null || file == null) {
            return false;
        }
        File parentFile = file.getParentFile();
        if (!mkdirs(parentFile)) {
            return false;
        }
        File tmpFile = new File(parentFile, file.getName() + ".tmp");
        OutputStream out = null;
        boolean saved = false;
        try {
            out = new FileOutputStream(tmpFile);
            writeString(out, text, charset);
            saved = true;
        } catch (IOException e) {
            AndroidLog.INSTANCE.e(TAG, "IOUtils:write string to file=" + file.getAbsolutePath() + " failed", e);
            saved = false;
        } finally {
            closeQuietly(out);
            if (saved && !tmpFile.renameTo(file)) {
                AndroidLog.INSTANCE.e(TAG, "IOUtils:rename file failed");
                saved = false;
            }
        }
        return saved;
    }

    /**
     * @param path delete the directory or file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void delete(File path) {
        if (path == null || !path.exists()) {
            return;
        }
        if (path.isFile()) {
            path.delete();
        } else {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    delete(file);
                }
            }
            path.delete();
        }
    }

    /**
     * @param dir the file dir
     */
    public static boolean mkdirs(File dir) {
        if (dir == null) {
            return false;
        }
        try {
            if (dir.exists()) {
                return dir.isDirectory() || (dir.delete() && dir.mkdirs());
            } else {
                return dir.mkdirs();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
