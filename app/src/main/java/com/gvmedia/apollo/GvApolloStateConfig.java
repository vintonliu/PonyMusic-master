package com.gvmedia.apollo;

import static com.gvmedia.apollo.GvApolloEnum.kSrs2_0;

/**
 * Created by vinton on 2017/10/18,0018.
 */

public class GvApolloStateConfig {
    public short[] mHLDelay;
    public short[] mHLTemp;
    public short[] mVolumeControlDelay;
    public short[][] mHighPassFilterState_E;
    public GvApolloStateConfig() {
        mHLDelay = new short[2 * 100];
        mHLTemp = new short[100 * 100];
        mVolumeControlDelay = new short[100];
        mHighPassFilterState_E = new short[kSrs2_0][100];
    }
}
