//
// Created by vinton on 2017/10/18,0018.
//

#ifndef PONYMUSIC_MASTER_GVMEDIAENGINE_H
#define PONYMUSIC_MASTER_GVMEDIAENGINE_H

#include "apollo_api.h"
#include "apollo.h"

#define MAX_INPUT_CHANNELS_NUM      kSrs2_0 // kSrs6_1
#define MAX_OUTPUT_CHANNELS_NUM     kSrs2_0

enum GvSettingId
{
    SETTING_CHANNEL_ID =1,
    SETTING_STATE_ID = 2,
    SETTING_AUDIO_ID = 3,
    SETTING_SOUND_EFFECT_ID = 4
};

typedef struct tAudioConfig
{
    int sampleRate;
    int channels;
} GvAudioConfig;

class GvMediaEngine {
public:
    GvMediaEngine();
    virtual ~GvMediaEngine();

    /**
     * @brief init gvmedia
     * @return 0 for successed, else failure
     */
    int initEngine();

    /**
     * @brief set gvmedia config
     * @param settingId see enum @GvSettingId
     * @param cfgObj config structure
     */
    void setSetting(int settingId, void* cfgObj);

    /**
     * @brief get gvmedia config
     * @param settingId see enum @GvSettingId
     * @param cfgObj config structure
     */
    void getSetting(int settingId, void* cfgObj);

    /**
     * @brief process audio data in pcm format
     * @param dataInput input data will be process
     * @param dataOutput output data have been processed
     * @param nSize input data size
     * @return 0 for successed, else failure
     */
    int process(short dataInput[MAX_INPUT_CHANNELS_NUM][kMaxBlockLength],
                short dataOutput[MAX_OUTPUT_CHANNELS_NUM][kMaxBlockLength],
                int nSize);

private:
    GVApolloChannel* _channelCfg;
    GVApolloState* _stateCfg;

    GvAudioConfig _audioConfig;

    Sample*	_tempSample;
};


#endif //PONYMUSIC_MASTER_GVMEDIAENGINE_H
