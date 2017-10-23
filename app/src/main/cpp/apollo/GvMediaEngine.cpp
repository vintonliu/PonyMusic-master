//
// Created by vinton on 2017/10/18,0018.
//

#include <stdio.h>
#include <string.h>
#include <android/log.h>
#include <sample.h>
#include "GvMediaEngine.h"

#define LOGI(...)   __android_log_print((int)ANDROID_LOG_INFO, "Apollo-jni", __VA_ARGS__)
#define LOGE(...)   __android_log_print((int)ANDROID_LOG_ERROR, "Apollo-jni", __VA_ARGS__)

GvMediaEngine::GvMediaEngine()
{
    LOGI("%s ctor", __func__);
    _channelCfg = new GVApolloChannel();
    _stateCfg = new GVApolloState();

    _audioConfig.sampleRate = 48000;
    _audioConfig.channels = 2;

    _effectConfig.id = 0;

    _tempSample = new Sample[kSrsApolloTempBuffers * kMaxBlockLength];
}

GvMediaEngine::~GvMediaEngine()
{
    LOGI("%s ~dtor", __func__);
    if (_tempSample)
    {
        delete _tempSample;
        _tempSample = NULL;
    }

    if (_channelCfg)
    {
        delete _channelCfg;
        _channelCfg = NULL;
    }

    if (_stateCfg)
    {
        delete _stateCfg;
        _stateCfg = NULL;
    }
}

void GvMediaEngine::setSetting(int settingId, void *cfgObj)
{
    LOGI("%s settingId %d", __func__, settingId);
    switch (settingId)
    {
        case SETTING_AUDIO_FORMAT_ID:
        {
            GvAudioFormatConfig* config = (GvAudioFormatConfig*)cfgObj;
            _audioConfig.sampleRate = config->sampleRate;
            _audioConfig.channels = config->channels;
            LOGI("%s sampleRate[%d] channels[%d]", __FUNCTION__,
                _audioConfig.sampleRate,
                _audioConfig.channels);
        }
        break;

        case SETTING_SOUND_EFFECT_ID:
        {
            _effectId = *(int*)cfgObj;
            GvSoundEffectConfig* config = (GvSoundEffectConfig*)cfgObj;
            _effectConfig.id = config->id;
        }
        break;

        default:
            break;
    }
}

void GvMediaEngine::getSetting(int settingId, void *cfgObj)
{
    LOGI("%s settingId %d", __func__, settingId);
    switch (settingId)
    {
        case SETTING_AUDIO_FORMAT_ID:
        {
            memcpy((GvAudioFormatConfig*)cfgObj, &_audioConfig, sizeof(GvAudioFormatConfig));
        }
        break;

        case SETTING_SOUND_EFFECT_ID:
        {
            cfgObj = (void*)&_effectId;
            memcpy((GvSoundEffectConfig*)cfgObj, &_effectConfig, sizeof(GvSoundEffectConfig));
        }
        break;

        default:
            break;
    }
}

int GvMediaEngine::initEngine()
{
    LOGI("%s", __func__);
    GVApolloChannelInit(_channelCfg);
    GVApolloStateInit(_channelCfg, _stateCfg);
    return 0;
}

int GvMediaEngine::processData(char *dataInput,
                               char *dataOutput,
                               int sizeInBytes)
{
//    LOGI("%s", __func__);

    memcpy(dataOutput, dataInput, sizeInBytes);

    return 0;
}
