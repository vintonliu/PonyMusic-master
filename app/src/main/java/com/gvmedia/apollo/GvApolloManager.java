package com.gvmedia.apollo;

import java.nio.ByteBuffer;

/**
 * Created by vinton on 2017/10/18,0018.
 */

public final class GvApolloManager {
    // Load the native library upon startup
    static {
        System.loadLibrary("apollo");
    }

    private static final String TAG = "GvApolloManager";

    private static final GvApolloManager ourInstance = new GvApolloManager();

    private native final static long newInstance();

    private native final void deleteInstance(long handle);

    private native final void setSetting(long handle, int settingId, Object cfgObj);

    private native final void getSetting(long handle, int settingId, Object cfgObj);

    private native final int apolloInit(long handle);

    private native final int apolloProcess(long handle, int nSize);

    private native final int getMaxBlockLen(long handle);

    private native final void cacheInputBufferDirectAddress(ByteBuffer byteBuffer);

    private native final void cacheOutputBufferDirectBuffer(ByteBuffer byteBuffer);

    /** GvMediaEngine instance handle **/
    long handle = 0;

    ByteBuffer inputBuffer;
    ByteBuffer outputBuffer;

    /**
     * get singleton instance
     * @return gvmedia instance
     */
    public static GvApolloManager getInstance() {
        return ourInstance;
    }

    private GvApolloManager() {
        handle = newInstance();

        // 500k bytes
        inputBuffer = ByteBuffer.allocateDirect(500 * 1024);
        outputBuffer = ByteBuffer.allocateDirect(500 * 1024);

        cacheInputBufferDirectAddress(inputBuffer);
        cacheOutputBufferDirectBuffer(outputBuffer);
    }

    /**
     * free gvmedia instance memory
     */
    public void GvClose() {
        if (handle != 0) {
            deleteInstance(handle);
        }
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
     * processData audio data in pcm format
     * @param dataInput input data will be processData
     * @param dataOutput output data have been processed
     * @param length input data size
     * @return 0 for successed, else failure
     */
    public int GvProcess(ByteBuffer dataInput,
                         byte[] dataOutput,
                         int length) {
        inputBuffer.rewind();
        inputBuffer.put(dataInput);
        int res = apolloProcess(handle, length);
        if (res != 0) {
            return res;
        }

        outputBuffer.get(dataOutput, 0, length);
        outputBuffer.rewind();
        return res;
    }

    public int GvGetMaxBlockLength() {
        return getMaxBlockLen(handle);
    }
}
