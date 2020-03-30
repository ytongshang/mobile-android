#include <jni.h>
#include <string>
#include <unistd.h>

#ifdef __cplusplus
extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavfilter/avfilter.h>
#include <libavcodec/jni.h>
#include "cradle_rancune_media_ffmpeg_FFmpeg2.h"
#endif

JNIEXPORT jstring JNICALL
Java_cradle_rancune_media_ffmpeg_FFmpeg2_getCoder(JNIEnv *env, jobject  /* this */) {

    char info[40000] = {0};
    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            sprintf(info, "%sdecode:", info);
            switch (c_temp->type) {
                case AVMEDIA_TYPE_VIDEO:
                    sprintf(info, "%s(video):", info);
                    break;
                case AVMEDIA_TYPE_AUDIO:
                    sprintf(info, "%s(audio):", info);
                    break;
                default:
                    sprintf(info, "%s(other):", info);
                    break;
            }
            sprintf(info, "%s[%10s]\n", info, c_temp->name);
        }
        c_temp = c_temp->next;
    }
    return env->NewStringUTF(info);
}

#ifdef __cplusplus
}
#endif