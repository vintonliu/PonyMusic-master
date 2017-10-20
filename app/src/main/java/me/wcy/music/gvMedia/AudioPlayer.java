package me.wcy.music.gvMedia;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;
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

public class AudioPlayer implements AudioDecoder.OnDecodeCallback{
    private final static String TAG = "AudioPlayer";
    private static int channels = 1;
    private final static int bitsPerSample = 16;
    private static int bytesPerSample = channels * (bitsPerSample / 8);
    /* per 10 ms callback, 1000 / 10 */
    private final static int buffersPerSecond = 100;
    private int sampleRate = 16000;
    private int maxBytesPerBuffer = bytesPerSample * 480;
    private int samplesPerBuffer = sampleRate / buffersPerSecond;

    private AudioTrack mAudioTrack = null;
    private ByteBuffer playBuffer = null;
    private AudioDecoder mAudioDecoder = null;
    private int playSizePerBuffer = 0;
//    private AudioTrackThread audioThread = null;
    private String mAudioPath = null;
    private boolean isPause = false;
    private boolean isPlaying = false;

    private class AudioTrackThread extends Thread {
        private volatile boolean keepAlive = true;
        private byte[] tmpBufPlay = null;

        public AudioTrackThread(String name) {
            super(name);
            tmpBufPlay = new byte[maxBytesPerBuffer];
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

            try {
                mAudioTrack.play();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            int bytesWritten;
            while(keepAlive){
                if (isPause()) {
                    continue;
                }

                if (playBuffer.remaining() < playSizePerBuffer) {
                    continue;
                }
                playBuffer.get(tmpBufPlay);
                bytesWritten = mAudioTrack.write(tmpBufPlay, 0, playSizePerBuffer);
                if (bytesWritten != playSizePerBuffer) {
                    Log.e(TAG, "AudioTrack write failed.");
                }
                playBuffer.rewind();
            }

            try {
                mAudioTrack.stop();
                mAudioTrack.flush();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        public void joinThread() {
            keepAlive = false;
            while (isAlive()) {
                try {
                    join(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AudioPlayer() {
        mAudioDecoder = new AudioDecoder();
        mAudioDecoder.setOnDecodeCallback(this);

        GvApolloManager.getInstance().GvInit();
    }

    @Override
    public void onDecode(ByteBuffer buffer, int offset, int length) {
        byte[] tmpBuf = new byte[length];
        buffer.get(tmpBuf);
//        GvApolloManager.getInstance().GvProcess();
        mAudioTrack.write(tmpBuf, 0, length);
    }

    public void setDataSource(String audioPath) throws IOException {
        final File file = new File(audioPath);
        if (!file.exists()) {
            throw new IOException("setDataSource failed.");
        }

        mAudioPath = audioPath;
        mAudioDecoder.setDataSource(audioPath);
    }

    public void prepareAsync() throws IllegalStateException {
//        audioThread = new AudioTrackThread("AudioPlayerThread");
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
    }

    private boolean initPlayout() {
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
            mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
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
        isPause = false;
        mAudioTrack.play();
//        audioThread.start();
    }

    public void pause() {
        isPause = true;
        mAudioTrack.pause();
    }

    public void stop() {
        mAudioTrack.stop();
    }

    public void seekTo(int msec) throws IllegalStateException {
        mAudioDecoder.seekTo(msec);
    }

    public int getCurrentPosition() {
        return 0;
    }

    public void reset() {
//        if (audioThread != null) {
//            audioThread.joinThread();
//            audioThread = null;
//        }
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

    public boolean isPause() {
        return isPause;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         *
         */
        void onCompletion(AudioPlayer ap);
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnCompletionListener(AudioPlayer.OnCompletionListener listener)
    {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    public interface OnPreparedListener
    {
        /**
         * Called when the media file is ready for playback.
         *
         * @param ap the MediaPlayer that is ready for playback
         */
        void onPrepared(AudioPlayer ap);
    }

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnPreparedListener(AudioPlayer.OnPreparedListener listener)
    {
        mOnPreparedListener = listener;
    }

    private AudioPlayer.OnPreparedListener mOnPreparedListener;
}
