package com.gvmedia.apollo;

/**
 * Created by vinton on 2017/10/18,0018.
 */

public class GvAudioFormatConfig {
    public int mSampleRate = 16000;
    public int mChannels = 2;

    public GvAudioFormatConfig(int sampleRate, int channels) {
        this.mSampleRate = sampleRate;
        this.mChannels = channels;
    }
}
