package me.wcy.music.gvMedia;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gvmedia.apollo.GvApolloAudioConfig;
import com.gvmedia.apollo.GvApolloEnum;
import com.gvmedia.apollo.GvApolloManager;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by vinton on 2017/10/20,0020.
 */

public class AudioPlayer implements AudioDecoder.OnDecodeCallback, AudioDecoder.OnCompletionListener{
    private final static String TAG = "AudioPlayer";

    private static final int ON_PREPARING_MSG = 1;
    private static final int ON_COMPLETION_MSG = 2;

    private static int channels = 2;
    private int sampleRate = 16000;

    private AudioTrack mAudioTrack = null;
    private AudioDecoder mAudioDecoder = null;

    public AudioPlayer() {
        mAudioDecoder = new AudioDecoder();
        mAudioDecoder.setOnDecodeCallback(this);
        mAudioDecoder.setOnCompletionListener(this);

        GvApolloManager.getInstance().GvInit();
    }

    public void setDataSource(String audioPath) throws IOException {
        final File file = new File(audioPath);
        if (!file.exists()) {
            throw new IOException("setDataSource failed.");
        }

        mAudioDecoder.setDataSource(audioPath);
    }

    public void prepareAsync() throws IllegalStateException {
        Log.i(TAG, "prepareAsync()");
        if (!mAudioDecoder.initDecoder()) {
            throw new IllegalStateException("audio decoder init failed.");
        }

        if (!initPlayout()) {
            throw new IllegalStateException("audio player init failed");
        }

        GvApolloAudioConfig audioConfig =
                new GvApolloAudioConfig(mAudioDecoder.getSampleRate(),
                                        mAudioDecoder.getChannels());
        GvApolloManager.getInstance().GvSetSetting(GvApolloEnum.SETTING_AUDIO_ID, audioConfig);

        mHandler.sendEmptyMessageDelayed(ON_PREPARING_MSG, 100);
    }

    private boolean initPlayout() {
        Log.i(TAG, "initPlayout()");
        sampleRate = mAudioDecoder.getSampleRate();
        channels = mAudioDecoder.getChannels();
        int channelConfig = channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
        int minBuffSize = AudioTrack.getMinBufferSize(sampleRate,
                                                    channelConfig,
                                                    AudioFormat.ENCODING_PCM_16BIT);

        if (minBuffSize < 6000) {
            minBuffSize *= 2;
        }

        try {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate,
                    channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuffSize,
                    AudioTrack.MODE_STREAM);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }

        if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "StartPlayout() failed to new AudioTrack");
            if (mAudioTrack != null) {
                mAudioTrack.release();
                mAudioTrack = null;
            }
            return false;
        }

        return true;
    }

    public void start() {
        Log.i(TAG, "start()");
        if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.play();
        }

        mAudioDecoder.start();
    }

    public void pause() {
        Log.i(TAG, "pause()");
        mAudioTrack.pause();
        mAudioDecoder.pause();
    }

    public void stop() {
        Log.i(TAG, "stop()");
        if (mAudioDecoder != null) {
            mAudioDecoder.stop();
        }

        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }

    public void seekTo(int msec) throws IllegalStateException {
        mAudioDecoder.seekTo(msec);
    }

    public int getCurrentPosition() {
        return mAudioDecoder.getCurrentPositionMs();
    }

    public void reset() {
        Log.i(TAG, "reset()");

        if (mAudioDecoder != null) {
            mAudioDecoder.stop();
        }

        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }

    public void release() {
        reset();
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }

        if (mAudioDecoder != null) {
            mAudioDecoder.stop();
            mAudioDecoder.release();
            mAudioDecoder = null;
        }

        GvApolloManager.getInstance().GvClose();
    }

    public boolean isPlaying() {
        return mAudioDecoder.isPlaying();
    }

    public boolean isPausing() {
        return mAudioDecoder.isPausing();
    }

    public boolean isPreparing() {
        return mAudioDecoder.isPreparing();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ON_PREPARING_MSG) {
                if (mOnPreparedListener != null) {
                    mOnPreparedListener.onPrepared();
                }
            } else if (msg.what == ON_COMPLETION_MSG) {
                if (mOnCompletionListener != null) {
                    mOnCompletionListener.onCompletion();
                }
            }
        }
    };

    @Override
    public void onDecode(ByteBuffer buffer, int offset, int length) {
        byte[] tmpBuf = new byte[length];
        buffer.get(tmpBuf);
//        GvApolloManager.getInstance().GvProcess();
        if (mAudioTrack != null) {
            mAudioTrack.write(tmpBuf, 0, length);
        }
    }

    @Override
    public void onCompletion(AudioDecoder ap) {
        mHandler.sendEmptyMessageDelayed(ON_COMPLETION_MSG, 100);
    }

    public interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         *
         */
        void onCompletion();
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnCompletionListener(AudioPlayer.OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    public interface OnPreparedListener {
        /**
         * Called when the media file is ready for playback.
         *
         */
        void onPrepared();
    }

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnPreparedListener(AudioPlayer.OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    private AudioPlayer.OnPreparedListener mOnPreparedListener;
}
