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
 * Signature: (J[[S[[SI)I
 */
JNIEXPORT jint JNICALL apolloProcess(JNIEnv *env,
                                     jobject thiz,
                                     jlong handle,
                                     jobjectArray dataInputArray,
                                     jobjectArray dataOutputArray,
                                     jint nSize);

static void printSettingName(const char* func, int settingId);

static JNINativeMethod g_native_methods[] = {
        {"newInstance", "()J", (void*)newInstance},
        {"deleteInstance", "(J)V", (void*)deleteInstance},
        {"setSetting", "(JILjava/lang/Object;)V", (void*)setSetting},
        {"getSetting", "(JILjava/lang/Object;)V", (void*)getSetting},
        {"apolloInit", "(J)I", (void*)apolloInit},
        {"apolloProcess", "(J[[S[[SI)I", (void*)apolloProcess}
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
        case SETTING_CHANNEL_ID:
        {

        }
        break;

        case SETTING_STATE_ID:
        {

        }
        break;

        case SETTING_AUDIO_ID:
        {
            GvAudioConfig config;
            memset(&config, 0x00, sizeof(GvAudioConfig));

            jfieldID jfidSampleRate = (env)->GetFieldID(jCls, "mSampleRate", "I");
            jfieldID jfidChannels = (env)->GetFieldID(jCls, "mChannels", "I");

            config.sampleRate = (env)->GetIntField(cfgObj, jfidSampleRate);
            config.channels = (env)->GetIntField(cfgObj, jfidChannels);
            ptr->setSetting(settingId, (void*)&config);
        }
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
        case SETTING_CHANNEL_ID:
        {

        }
            break;

        case SETTING_STATE_ID:
        {

        }
            break;

        case SETTING_AUDIO_ID:
        {
            GvAudioConfig config;
            memset(&config, 0x00, sizeof(GvAudioConfig));

            ptr->getSetting(settingId, (void*)&config);

            jfieldID jfidSampleRate = (env)->GetFieldID(jCls, "mSampleRate", "I");
            jfieldID jfidChannels = (env)->GetFieldID(jCls, "mChannels", "I");

            (env)->SetIntField(cfgObj, jfidSampleRate, config.sampleRate);
            (env)->SetIntField(cfgObj, jfidChannels, config.channels);
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
 * Signature: (J[[S[[SI)I
 */
JNIEXPORT jint JNICALL apolloProcess(JNIEnv *env,
                                     jobject thiz,
                                     jlong handle,
                                     jobjectArray dataInputArray,
                                     jobjectArray dataOutputArray,
                                     jint nSize)
{
    LOGI("%s", __func__);
    GvMediaEngine* ptr = (GvMediaEngine*)handle;
    if (NULL == ptr)
    {
        LOGE("%s GvMedia Engine has destory.", __FUNCTION__);
        return JNI_ERR;
    }

    return ptr->process(NULL, NULL, 0);
}

void printSettingName(const char* func, int settingId)
{
    switch (settingId)
    {
        case SETTING_CHANNEL_ID:
            LOGI("%s SETTING_CHANNEL_ID", func);
            return;
        case SETTING_STATE_ID:
            LOGI("%s SETTING_STATE_ID", func);
            return;
        case SETTING_AUDIO_ID:
            LOGI("%s SETTING_AUDIO_ID", func);
            return;
        default:
            LOGI("%s UNKNOWN", func);
            return;
    }
}

#ifdef __cplusplus
}
#endif
