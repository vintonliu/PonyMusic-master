//
// Created by vinton on 2017/10/18,0018.
//

#include <jni.h>
#include <android/log.h>
#include <GvMediaEngine.h>
#include <string.h>

#define LOGI(...)   __android_log_print((int)ANDROID_LOG_INFO, "Apollo-jni", __VA_ARGS__)
#define LOGE(...)   __android_log_print((int)ANDROID_LOG_ERROR, "Apollo-jni", __VA_ARGS__)


#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    newInstance
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL newInstance(JNIEnv *env, jclass cls);

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    deleteInstance
 * Signature: (J)V
 */
JNIEXPORT void JNICALL deleteInstance(JNIEnv *env, jobject thiz, jlong handle);

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    setSetting
 * Signature: (JILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL setSetting(JNIEnv *env, jobject thiz,
                                  jlong handle, jint settingId,
                                  jobject cfgObj);

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    getSetting
 * Signature: (JILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL getSetting(JNIEnv *env, jobject thiz,
                                  jlong handle, jint settingId,
                                  jobject cfgObj);

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    apolloInit
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL apolloInit(JNIEnv *env, jobject thiz, jlong handle);

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    apolloProcess
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL apolloProcess(JNIEnv *env,
                                     jobject thiz,
                                     jlong handle,
                                     jint sizeInBytes);

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    getMaxBlockLen
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL getMaxBlockLen(JNIEnv *env, jobject thiz, jlong handle);

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    cacheInputBufferDirectAddress
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL cacheInputBufferDirectAddress
        (JNIEnv* env, jobject thiz, jobject inBuffer);

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    cacheOutputBufferDirectBuffer
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL cacheOutputBufferDirectBuffer
        (JNIEnv* env, jobject thiz, jobject outBuffer);

static void printSettingName(const char* func, int settingId);

static void* inbuf_direct_address = NULL;
static void* outbuf_direct_address = NULL;

static JNINativeMethod g_native_methods[] = {
        {"newInstance", "()J", (void*)newInstance},
        {"deleteInstance", "(J)V", (void*)deleteInstance},
        {"setSetting", "(JILjava/lang/Object;)V", (void*)setSetting},
        {"getSetting", "(JILjava/lang/Object;)V", (void*)getSetting},
        {"apolloInit", "(J)I", (void*)apolloInit},
        {"apolloProcess", "(JI)I", (void*)apolloProcess},
        {"getMaxBlockLen", "(J)I", (void*)getMaxBlockLen},
        {"cacheInputBufferDirectAddress", "(Ljava/nio/ByteBuffer;)V", (void*)cacheInputBufferDirectAddress},
        {"cacheOutputBufferDirectBuffer", "(Ljava/nio/ByteBuffer;)V", (void*)cacheOutputBufferDirectBuffer}
};

/*
 * Class:     com_audio_apollo_ApolloManager
 * Method:    JNI_OnLoad
 * Signature: ()I
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* pEnv = NULL;

    if ((*vm).GetEnv((void**)&pEnv, JNI_VERSION_1_6) != JNI_OK)
    {
        LOGE("JNI_OnLoad() failed to get env.");

        return JNI_ERR;
    }

    jclass cls = pEnv->FindClass("com/gvmedia/apollo/GvApolloManager");
    if (NULL == cls)
    {
        LOGE("JNI_OnLoad() failed to find class \"com.audio.apollo.ApolloManager\".");
        return JNI_ERR;
    }

    jint res = pEnv->RegisterNatives(cls,
                                     g_native_methods,
                                     sizeof(g_native_methods) / sizeof(g_native_methods[0]));
    if (res != JNI_OK)
    {
        LOGE("JNI_OnLoad() failed to RegisterNatives.");
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    newInstance
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL newInstance(JNIEnv *env,
                                    jclass cls)
{
    LOGI("%s", __func__);
    return (jlong )(new GvMediaEngine());
}

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    deleteInstance
 * Signature: (J)V
 */
JNIEXPORT void JNICALL deleteInstance(JNIEnv *env,
                                      jobject thiz,
                                      jlong handle)
{
    LOGI("%s", __func__);
    GvMediaEngine* ptr = (GvMediaEngine*)handle;
    if (NULL == ptr)
    {
        LOGE("%s GvMedia Engine has destory.", __FUNCTION__);
        return;
    }

    delete ptr;
}

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    setSetting
 * Signature: (JILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL setSetting(JNIEnv *env,
                                  jobject thiz,
                                  jlong handle,
                                  jint settingId,
                                  jobject cfgObj)
{
    GvMediaEngine* ptr = (GvMediaEngine*)handle;
    if (NULL == ptr)
    {
        LOGE("%s GvMedia Engine has destory.", __FUNCTION__);
        return;
    }

    printSettingName(__FUNCTION__, settingId);

    jclass jCls = (jclass)(env)->GetObjectClass(cfgObj);
    if (NULL == jCls)
    {
        LOGE("%s couldn't get object class.", __FUNCTION__);
        return;
    }

    switch (settingId)
    {
        case SETTING_AUDIO_FORMAT_ID:
        {
            GvAudioFormatConfig config;
            memset(&config, 0x00, sizeof(GvAudioFormatConfig));

            jfieldID jfidSampleRate = (env)->GetFieldID(jCls, "mSampleRate", "I");
            jfieldID jfidChannels = (env)->GetFieldID(jCls, "mChannels", "I");

            config.sampleRate = (env)->GetIntField(cfgObj, jfidSampleRate);
            config.channels = (env)->GetIntField(cfgObj, jfidChannels);
            ptr->setSetting(settingId, (void*)&config);
        }
        break;

        case SETTING_SOUND_EFFECT_ID:
        {
            GvSoundEffectConfig config;
            jfieldID jfidEffectId = (env)->GetFieldID(jCls, "mSoundEffectId", "I");
            config.id = (env)->GetIntField(cfgObj, jfidEffectId);
            ptr->setSetting(SETTING_SOUND_EFFECT_ID, (void*)&config);
            LOGI("%s soundEffectId = %d", __FUNCTION__, config.id);
        }
        break;

        default:
            break;
    }
}

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    getSetting
 * Signature: (JILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL getSetting(JNIEnv *env,
                                  jobject thiz,
                                  jlong handle,
                                  jint settingId,
                                  jobject cfgObj)
{
    GvMediaEngine* ptr = (GvMediaEngine*)handle;
    if (NULL == ptr)
    {
        LOGE("%s GvMedia Engine has destory.", __FUNCTION__);
        return;
    }
    printSettingName(__FUNCTION__, settingId);

    jclass jCls = (jclass)(env)->GetObjectClass(cfgObj);
    if (NULL == jCls)
    {
        LOGE("%s couldn't get object class.", __FUNCTION__);
        return;
    }

    switch (settingId)
    {
        case SETTING_AUDIO_FORMAT_ID:
        {
            GvAudioFormatConfig config;
            memset(&config, 0x00, sizeof(GvAudioFormatConfig));

            ptr->getSetting(settingId, (void*)&config);

            jfieldID jfidSampleRate = (env)->GetFieldID(jCls, "mSampleRate", "I");
            jfieldID jfidChannels = (env)->GetFieldID(jCls, "mChannels", "I");

            (env)->SetIntField(cfgObj, jfidSampleRate, config.sampleRate);
            (env)->SetIntField(cfgObj, jfidChannels, config.channels);
        }
        break;

        case SETTING_SOUND_EFFECT_ID:
        {
            GvSoundEffectConfig config;

            ptr->getSetting(SETTING_SOUND_EFFECT_ID, (void*)&config);
            jfieldID jfidEffectId = (env)->GetFieldID(jCls, "mSoundEffectId", "I");
            (env)->SetIntField(cfgObj, jfidEffectId, config.id);
        }
        break;
    }
}

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    apolloInit
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL apolloInit(JNIEnv *env,
                                  jobject thiz,
                                  jlong handle)
{
    LOGI("%s", __func__);
    GvMediaEngine* ptr = (GvMediaEngine*)handle;
    if (NULL == ptr)
    {
        LOGE("%s GvMedia Engine has destory.", __FUNCTION__);
        return JNI_ERR;
    }

    return ptr->initEngine();
}

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    apolloProcess
 * Signature: ((JI)I
 */
JNIEXPORT jint JNICALL apolloProcess(JNIEnv *env,
                                     jobject thiz,
                                     jlong handle,
                                     jint sizeInBytes)
{
//    LOGI("%s", __func__);
    GvMediaEngine* ptr = (GvMediaEngine*)handle;
    if (NULL == ptr)
    {
        LOGE("%s GvMedia Engine has destory.", __FUNCTION__);
        return JNI_ERR;
    }

    int res = ptr->processData((char*)inbuf_direct_address,
                               (char*)outbuf_direct_address,
                               sizeInBytes);
    if (res != 0)
    {
        return res;
    }

    return 0;
}

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    getMaxBlockLen
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL getMaxBlockLen(JNIEnv *env, jobject thiz, jlong handle)
{
    return kMaxBlockLength;
}

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    cacheInputBufferDirectAddress
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL cacheInputBufferDirectAddress
        (JNIEnv* env, jobject thiz, jobject inBuffer)
{
    inbuf_direct_address = (env)->GetDirectBufferAddress(inBuffer);
}

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    cacheOutputBufferDirectBuffer
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL cacheOutputBufferDirectBuffer
        (JNIEnv* env, jobject thiz, jobject outBuffer)
{
    outbuf_direct_address = (env)->GetDirectBufferAddress(outBuffer);
}

void printSettingName(const char* func, int settingId)
{
    switch (settingId)
    {
        case SETTING_AUDIO_FORMAT_ID:
            LOGI("%s SETTING_AUDIO_FORMAT_ID", func);
            return;
        case SETTING_SOUND_EFFECT_ID:
            LOGI("%s SETTING_SOUND_EFFECT_ID", func);
            return;
        default:
            LOGI("%s UNKNOWN", func);
            return;
    }
}

#ifdef __cplusplus
}
#endif
