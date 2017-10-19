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

/*
 * Class:     com_gvmedia_apollo_GvApolloManager
 * Method:    getMaxBlockLen
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL getMaxBlockLen(JNIEnv *env, jobject thiz, jlong handle);

static void printSettingName(const char* func, int settingId);

static JNINativeMethod g_native_methods[] = {
        {"newInstance", "()J", (void*)newInstance},
        {"deleteInstance", "(J)V", (void*)deleteInstance},
        {"setSetting", "(JILjava/lang/Object;)V", (void*)setSetting},
        {"getSetting", "(JILjava/lang/Object;)V", (void*)getSetting},
        {"apolloInit", "(J)I", (void*)apolloInit},
        {"apolloProcess", "(J[[S[[SI)I", (void*)apolloProcess},
        {"getMaxBlockLen", "(J)I", (void*)getMaxBlockLen}
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

        case SETTING_SOUND_EFFECT_ID:
        {
            jfieldID jfidEffectId = (env)->GetFieldID(jCls, "mSoundEffectId", "I");

            LOGI("%s soundEffectId = %d", __FUNCTION__,
                 (env)->GetIntField(cfgObj, jfidEffectId));
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

        case SETTING_SOUND_EFFECT_ID:
        {
            jfieldID jfidEffectId = (env)->GetFieldID(jCls, "mSoundEffectId", "I");
            (env)->SetIntField(cfgObj, jfidEffectId, 2);
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

    short inputData[MAX_INPUT_CHANNELS_NUM][kMaxBlockLength] = { 0 };
    short outputData[MAX_OUTPUT_CHANNELS_NUM][kMaxBlockLength] = { 0 };

    int inputRow = (env)->GetArrayLength(dataInputArray);
    inputRow = inputRow > MAX_INPUT_CHANNELS_NUM ? MAX_INPUT_CHANNELS_NUM : inputRow;
    jarray inputArray = (jarray )(env)->GetObjectArrayElement(dataInputArray, 0);
    int inputCol = (env)->GetArrayLength(inputArray);
    inputCol = inputCol > kMaxBlockLength ? kMaxBlockLength : inputCol;
    LOGI("%s inputRow = %d inputCol = %d", __FUNCTION__, inputRow, inputCol);

    for (int rowIndex = 0; rowIndex < inputRow; rowIndex++)
    {
        inputArray = (jarray)(env)->GetObjectArrayElement(dataInputArray, rowIndex);
        jshort* coldata = (env)->GetShortArrayElements((jshortArray)inputArray, 0);
        for (int colIndex = 0; colIndex < inputCol; ++colIndex)
        {
            inputData[rowIndex][colIndex] = coldata[colIndex];
        }
        (env)->ReleaseShortArrayElements((jshortArray)inputArray, coldata, 0);
    }

    int res = ptr->process(inputData, outputData, nSize);
    if (res != 0)
    {
        return res;
    }

    int outputRow = (env)->GetArrayLength(dataOutputArray);
    outputRow = outputRow > MAX_OUTPUT_CHANNELS_NUM ? MAX_OUTPUT_CHANNELS_NUM : outputRow;
    jarray outArray = (jarray)(env)->GetObjectArrayElement(dataOutputArray, 0);
    int outCol = (env)->GetArrayLength(outArray);
    outCol = outCol > kMaxBlockLength ? kMaxBlockLength : outCol;
    LOGI("%s outputRow = %d outCol = %d", __FUNCTION__, outputRow, outCol);
    for (int rowIndex = 0; rowIndex < outputRow; ++rowIndex)
    {
        outArray = (jarray)(env)->GetObjectArrayElement(dataOutputArray, rowIndex);
        (env)->SetShortArrayRegion((jshortArray)outArray, 0, kMaxBlockLength, outputData[rowIndex]);
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
