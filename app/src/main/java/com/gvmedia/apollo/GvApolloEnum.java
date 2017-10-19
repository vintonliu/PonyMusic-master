package com.gvmedia.apollo;

/**
 * Created by vinton on 2017/10/18,0018.
 */

public class GvApolloEnum {
    /* Global Decoding Block Size */
    public final static int kMaxBlockLength = 256;

    /* Number of channels for various formats */
    public final static int kSrsMono = 1;
    public final static int kSrs2_0 = 2;
    public final static int kSrsLRC = 3;
    public final static int kSrsLRCS = 4;
    public final static int kSrs5_0 = 5;
    public final static int kSrs5_1 = 6;
    public final static int kSrs6_1 = 7;
    public final static int kSrs7_1 = 8;

    /* Apollo Input Modes */
    public final static int kSrsApolloInput1_0_0 = 1;	/* C */
    public final static int kSrsApolloInput2_0_0 = 2;	/* L/R */
    public final static int kSrsApolloInputLtRt  = 3;	/* Lt/Rt */

    /* Apollo setting Id */
    public final static int SETTING_CHANNEL_ID = 1;
    public final static int SETTING_STATE_ID = 2;
    public final static int SETTING_AUDIO_ID = 3;
    public final static int SETTING_SOUND_EFFECT_ID = 4;
}
