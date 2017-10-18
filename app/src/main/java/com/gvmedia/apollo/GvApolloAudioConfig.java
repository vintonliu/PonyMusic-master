package com.gvmedia.apollo;

/**
 * Created by vinton on 2017/10/18,0018.
 */

public class GvApolloAudioConfig {
    public int mSampleRate = 16000;
    public int mChannels = 2;

    public GvApolloAudioConfig(int sampleRate, int channels) {
        this.mSampleRate = sampleRate;
        this.mChannels = channels;
    }
}
