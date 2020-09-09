package cradle.rancune.media.opengl.core;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import cradle.rancune.internal.logger.L;


/**
 * Created by Rancune@126.com 2018/7/23.
 */
@SuppressWarnings("WeakerAccess")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class EglHelper {

    private static final String TAG = "EglHelper";

    /**
     * init flag: surface must be recordable.  This discourages EGL from using a
     * pixel format that cannot be converted efficiently to something usable by the video
     * encoder.
     */
    public static final int FLAG_RECORDABLE = 0x01;

    /**
     * init flag: ask for GLES3, fall back to GLES2 if not available.  Without this
     * flag, GLES2 is used.
     */
    public static final int FLAG_TRY_GLES3 = 0x02;

    // Android-specific extension.
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private EGLDisplay mDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mSurface = EGL14.EGL_NO_SURFACE;
    private EGLConfig mConfig;

    private int mGlVersion = -1;

    public static void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + " : egl error: 0x"
                    + Integer.toHexString(error));
        }
    }

    public void init(EGLContext sharedContext, int flags) {
        if (sharedContext == null) {
            sharedContext = EGL14.EGL_NO_CONTEXT;
        }
        mDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mDisplay, version, 0, version, 1)) {
            throw new RuntimeException("unable to initialize EGL14");
        }
        if ((flags & FLAG_TRY_GLES3) != 0) {
            EGLConfig config = getConfig(flags, 3);
            if (config != null) {
                int[] attrib3_list = {
                        EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                        EGL14.EGL_NONE
                };
                EGLContext context = EGL14.eglCreateContext(mDisplay, config, sharedContext,
                        attrib3_list, 0);

                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    L.INSTANCE.d(TAG, "Got GLES 3 config");
                    mConfig = config;
                    mContext = context;
                    mGlVersion = 3;
                }
            }
        }
        // GLES 2 only, or GLES 3 attempt failed
        if (mContext == EGL14.EGL_NO_CONTEXT) {
            L.INSTANCE.d(TAG, "Trying GLES 2");
            EGLConfig config = getConfig(flags, 2);
            if (config == null) {
                throw new RuntimeException("Unable to find a suitable EGLConfig");
            }
            int[] attrib2_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            EGLContext context = EGL14.eglCreateContext(mDisplay, config, sharedContext,
                    attrib2_list, 0);
            checkEglError("eglCreateContext");
            mConfig = config;
            mContext = context;
            mGlVersion = 2;
        }

        // Confirm with query.
        int[] values = new int[1];
        EGL14.eglQueryContext(mDisplay, mContext, EGL14.EGL_CONTEXT_CLIENT_VERSION,
                values, 0);
        L.INSTANCE.d(TAG, "EGLContext created, client version " + values[0]);
    }

    public void createWindowSurface(Object surface) {
        if (!(surface instanceof Surface)
                && !(surface instanceof SurfaceView)
                && !(surface instanceof SurfaceHolder)
                && !(surface instanceof SurfaceTexture)) {
            throw new RuntimeException("invalid surface: " + surface);
        }
        int[] surfaceAttrs = {EGL14.EGL_NONE};
        mSurface = EGL14.eglCreateWindowSurface(mDisplay, mConfig, surface, surfaceAttrs, 0);
        checkEglError("eglCreateWindowSurface");
        if (mSurface == null) {
            throw new RuntimeException("surface was null");
        }
        EGL14.eglMakeCurrent(mDisplay, mSurface, mSurface, mContext);
        checkEglError("eglMakeCurrent");
    }

    public void createOffscreenSurface(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new RuntimeException("eglCreatePbufferSurface, but width or height < 0");
        }
        int[] pbAttrs = {
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE};
        mSurface = EGL14.eglCreatePbufferSurface(mDisplay, mConfig, pbAttrs, 0);
        checkEglError("eglCreatePbufferSurface");
        if (mSurface == null) {
            throw new RuntimeException("surface was null");
        }
        EGL14.eglMakeCurrent(mDisplay, mSurface, mSurface, mContext);
        checkEglError("eglMakeCurrent");
    }

    public void pause() {

    }

    public void destroy() {
        EGL14.eglMakeCurrent(mDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(mDisplay, mSurface);
        mSurface = EGL14.EGL_NO_SURFACE;
        EGL14.eglDestroyContext(mDisplay, mContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mDisplay);
        mDisplay = EGL14.EGL_NO_DISPLAY;
        mContext = EGL14.EGL_NO_CONTEXT;
        mConfig = null;
    }

    public void swapBuffers() {
        if (mDisplay != null && mSurface != null) {
            EGL14.eglSwapBuffers(mDisplay, mSurface);
        }
    }

    public void setPresentationTime(long presentationTime) {
        if (null != mDisplay && null != mSurface) {
            EGLExt.eglPresentationTimeANDROID(mDisplay, mSurface, presentationTime);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mDisplay != EGL14.EGL_NO_DISPLAY) {
                // We're limited here -- finalizers don't run on the thread that holds
                // the EGL state, so if a surface or context is still current on another
                // thread we can't fully release it here.  Exceptions thrown from here
                // are quietly discarded.  Complain in the log file.
                L.INSTANCE.w(TAG, "WARNING: EglCore was not explicitly released -- state may be leaked");
                destroy();
            }
        } finally {
            super.finalize();
        }
    }

    private EGLConfig getConfig(int flags, int version) {
        int renderableType = EGL14.EGL_OPENGL_ES2_BIT;
        if (version >= 3) {
            renderableType |= EGLExt.EGL_OPENGL_ES3_BIT_KHR;
        }

        // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                //EGL14.EGL_DEPTH_SIZE, 16,
                //EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, renderableType,
                EGL14.EGL_NONE, 0,      // placeholder for recordable [@-3]
                EGL14.EGL_NONE
        };
        if ((flags & FLAG_RECORDABLE) != 0) {
            attribList[attribList.length - 3] = EGL_RECORDABLE_ANDROID;
            attribList[attribList.length - 2] = 1;
        }
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            L.INSTANCE.w(TAG, "unable to find RGB8888 / " + version + " EGLConfig");
            return null;
        }
        return configs[0];
    }
}
