package com.gvmedia.apollo;

import static com.gvmedia.apollo.GvApolloEnum.kSrs6_1;

/**
 * Created by vinton on 2017/10/18,0018.
 */

public final class GvApolloManager {
    // Load the native library upon startup
    static {
        System.loadLibrary("apollo");
    }

    private static final GvApolloManager ourInstance = new GvApolloManager();

    private native final static long newInstance();

    private native final void deleteInstance(long handle);

    private native final void setSetting(long handle, int settingId, Object cfgObj);

    private native final void getSetting(long handle, int settingId, Object cfgObj);

    private native final int apolloInit(long handle);

    private native final int apolloProcess(long handle,
                                           short[][] dataInput,
                                           short[][] dataOutput,
                                           int nSize);

    private native final int getMaxBlockLen(long handle);

    /** GvMediaEngine instance handle **/
    long handle = 0;

    /**
     * get singleton instance
     * @return gvmedia instance
     */
    public static GvApolloManager getInstance() {
        return ourInstance;
    }

    private GvApolloManager() {
        handle = newInstance();
    }

    /**
     * free gvmedia instance memory
     */
    public void GvClose() {
        deleteInstance(handle);
    }

    /**
     * set gvmedia config
     * @param settingId see @GvApolloEnum Apollo setting Id
     * @param cfgObj config object
     */
    public void GvSetSetting(int settingId, Object cfgObj) {
        setSetting(handle, settingId, cfgObj);
    }

    /**
     * get gvmedia config
     * @param settingId see @GvApolloEnum Apollo setting Id
     * @param cfgObj config object
     */
    public void GvGetSetting(int settingId, Object cfgObj) {
        getSetting(handle, settingId, cfgObj);
    }

    /**
     * init gvmedia
     * @return 0 for successed, else failure
     */
    public int GvInit() {
        return apolloInit(handle);
    }

    /**
     * process audio data in pcm format
     * @param dataInput input data will be process
     * @param dataOutput output data have been processed
     * @param nSize input data size
     * @return 0 for successed, else failure
     */
    public int GvProcess(short[][] dataInput,
                         short[][] dataOutput,
                         int nSize) {
        return apolloProcess(handle, dataInput, dataOutput, nSize);
    }

    public int GvGetMaxBlockLength() {
        return getMaxBlockLen(handle);
    }
}
