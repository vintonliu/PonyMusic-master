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
        case SETTING_AUDIO_ID:
        {
            GvAudioConfig* config = (GvAudioConfig*)cfgObj;
            _audioConfig.sampleRate = config->sampleRate;
            _audioConfig.channels = config->channels;
            LOGI("%s sampleRate[%d] channels[%d]", __FUNCTION__,
                _audioConfig.sampleRate,
                _audioConfig.channels);
        }
        break;
    }
}

void GvMediaEngine::getSetting(int settingId, void *cfgObj)
{
    LOGI("%s settingId %d", __func__, settingId);
    switch (settingId)
    {
        case SETTING_AUDIO_ID:
        {
            _audioConfig.sampleRate = 44100;
            _audioConfig.channels = 1;
            memcpy((GvAudioConfig*)cfgObj, &_audioConfig, sizeof(GvAudioConfig));
        }
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

int GvMediaEngine::process(short dataInput[MAX_INPUT_CHANNELS_NUM][kMaxBlockLength],
                           short dataOutput[MAX_OUTPUT_CHANNELS_NUM][kMaxBlockLength],
                           int nSize)
{
    LOGI("%s", __func__);
    for (int i = 0; i < MAX_INPUT_CHANNELS_NUM; ++i) {
        for (int j = 0; j < kMaxBlockLength; ++j) {
            dataOutput[i][j] = dataInput[i][j];
        }
    }

    return 0;
}
