package org.webrtc.voiceengine;

import android.util.Log;

/**
 * Created by Vinton on 2014/10/13 0013.
 */
public class AudioDeviceParam {
    final private static String TAG = "AudioDeviceParam";

    public static AudioDeviceParam mSingle = null;
    public static AudioDevConfig stADevCfg = null;
    /* true to use dynamic policy, also easy for debug later */
    public static boolean bDynamicPolicyEn = false;

    public static AudioDeviceParam getInstance() {
        if ( mSingle == null ) {
            mSingle = new AudioDeviceParam();
            Log.i(TAG, "getInstance: Create!!");
        } else {
            Log.i(TAG, "getInstance: Single already exist!!");
        }

        return mSingle;
    }

    private AudioDeviceParam () {
        stADevCfg = new AudioDevConfig();
        stADevCfg.recordsource = 0;      // default 0 => DEFAULT
        stADevCfg.recordchannel = 0;     // default 0 => CHANNEL_CONFIGURATION_MONO
        stADevCfg.recordsamplerate = 1;  // default 1 =>16000
        stADevCfg.playstreamtype = 1;    // default 1 => STREAM_VOICE_CALL
        stADevCfg.playchannel = 0;       // default 0 => CHANNEL_CONFIGURATION_MONO
        stADevCfg.playsamplerate = 1;    // default 1 => 16000
        stADevCfg.speakermode = -1;      // default -1 => system current
        stADevCfg.earpiecemode = -1;     // default -1 => system current
        stADevCfg.callmode = -1;         // default -1 => system current
    }

    /**
     * Api for sdk, to set audio device param
     * @param devCfg
     */
    public synchronized void setAudioDevCfg(AudioDevConfig devCfg) {
        if ( devCfg != null ) {
            stADevCfg = devCfg;
        }
    }

    /**
     * Api for sdk, to set dynamic policy enable or not
     * @param enable
     */
    public synchronized void setDynamicPolicyEnable(boolean enable) {
        bDynamicPolicyEn = enable;
    }

    /**
     * Get dynamic policy enable
     * @return true for enable
     */
    public boolean getDynamicPolicyEnable() {
        return bDynamicPolicyEn;
    }

    /**
     * Api for sdk, to get current audio device param
     */
    public synchronized AudioDevConfig getAudioDevCfg() {
        return stADevCfg;
    }

    /**
     * Get record audio source
     * @return record audio source
     */
    public int getRecordSource() {
        return stADevCfg.recordsource;
    }

    /**
     * Get record channel
     * @return record channel
     */
    public int getRecordChannel() {
        return stADevCfg.recordchannel;
    }

    /**
     * Get record sample rate
     * @return record sample rate
     */
    public int getRecordSampleRate() {
        return stADevCfg.recordsamplerate;
    }

    /**
     * Get playback stream type
     * @return playback stream type
     */
    public int getPlayStreamType() {
        return stADevCfg.playstreamtype;
    }

    /**
     * Get playback channel
     * @return playback channel
     */
    public int getPlayChannel() {
        return stADevCfg.playchannel;
    }

    /**
     * Get playbck sample rate
     * @return playbck sample rate
     */
    public int getPlaySampleRate() {
        return stADevCfg.playsamplerate;
    }

    /**
     * Get playback speaker mode
     * @return playback speaker mode
     */
    public int getSpeakerMode() {
        return stADevCfg.speakermode;
    }

    /**
     * Get playback earpiece mode
     * @return playback earpiece mode
     */
    public int getEarpieceMode() {
        return stADevCfg.earpiecemode;
    }

    /**
     * Get playback call mode
     * @return playback call mode
     */
    public int getCallMode() {
        return stADevCfg.callmode;
    }

    public class AudioDevConfig extends Object {
        public int recordsource;       // audio record source
        public int recordchannel;      // audio record channel, mono or stereo
        public int recordsamplerate;   // audio record sample rate
        public int playstreamtype;     // audio playback stream type
        public int playchannel;        // audio playback channel, mono or stereo
        public int playsamplerate;     // audio playback sample rate
        public int speakermode;        // audio playback audio mode for speaker
        public int earpiecemode;       // audio playback audio mode for earpiece
        public int callmode;           // audio playback audio mode for normal call
    }
}
