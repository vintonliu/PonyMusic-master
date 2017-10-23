//
// Created by vinton on 2017/10/18,0018.
//

#ifndef PONYMUSIC_MASTER_GVMEDIAENGINE_H
#define PONYMUSIC_MASTER_GVMEDIAENGINE_H

#include "apollo_api.h"
#include "apollo.h"

enum GvSettingId
{
    SETTING_AUDIO_FORMAT_ID = 1,
    SETTING_SOUND_EFFECT_ID = 2
};

enum GvSoundEffectId
{
    GV_SOUND_EFFECT_NOTHING = 0,
    GV_SOUND_EFFECT_CLASSIC = 6,
};

typedef struct tAudioFormatConfig
{
    int sampleRate;
    int channels;
} GvAudioFormatConfig;

typedef struct tSoundEffectConfig
{
    int id;
}GvSoundEffectConfig;

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
     * @param sizeInBytes input data size
     * @return 0 for successed, else failure
     */
    int processData(char *dataInput,
                    char *dataOutput,
                    int sizeInBytes);

private:
    GVApolloChannel* _channelCfg;
    GVApolloState* _stateCfg;

    GvAudioFormatConfig _audioConfig;

    int _effectId;
    GvSoundEffectConfig _effectConfig;

    Sample*	_tempSample;
};


#endif //PONYMUSIC_MASTER_GVMEDIAENGINE_H
