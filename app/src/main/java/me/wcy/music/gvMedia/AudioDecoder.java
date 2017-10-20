package me.wcy.music.gvMedia;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.gvmedia.apollo.GvApolloAudioConfig;
import com.gvmedia.apollo.GvApolloEnum;
import com.gvmedia.apollo.GvApolloManager;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by vinton on 2017/10/20,0020.
 */

public class AudioDecoder {
    private final static String TAG = "AudioDecoder";

    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;
    private int mSampleRate;
    private int mChannels;
    private int mDuration;
    private String codecMime;
    private MediaCodec.BufferInfo decoderInfo;
    private ByteBuffer[] decodeInputBuffers;
    private ByteBuffer[] decodeOutputBuffers;

    private AudioDecodeThread mDecodeThread;

    private static String mAudioPath;
    private boolean isPause = false;
    private boolean isPlaying = false;

    class AudioDecodeThread extends Thread{
        private static final int TIMEOUT_US = 1000;
        private volatile boolean keepAlive = true;
        public AudioDecodeThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (keepAlive) {
                if (isPause) {
                    continue;
                }

                int inIndex = mDecoder.dequeueInputBuffer(TIMEOUT_US);
                if (inIndex >= 0) {
                    ByteBuffer buffer = decodeInputBuffers[inIndex];
                    //从MediaExtractor中读取一帧待解数据
                    int sampleSize = mExtractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        // We shouldn't stop the playback at this point, just pass the EOS
                        // flag to mDecoder, we will get it again from the
                        // dequeueOutputBuffer
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        mDecoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                    } else {
                        //向MediaDecoder输入一帧待解码数据
                        mDecoder.queueInputBuffer(inIndex, 0, sampleSize, mExtractor.getSampleTime(), 0);
                        mExtractor.advance();
                    }

                    //从MediaDecoder队列取出一帧解码后的数据
                    int outIndex = mDecoder.dequeueOutputBuffer(decoderInfo, TIMEOUT_US);
                    switch (outIndex) {
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                            decodeOutputBuffers = mDecoder.getOutputBuffers();
                            break;

                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            MediaFormat format = mDecoder.getOutputFormat();
                            Log.d(TAG, "New format " + format);
//                            audioTrack.setPlaybackRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE));

                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            Log.d(TAG, "dequeueOutputBuffer timed out!");
                            break;

                        default:
                            ByteBuffer outBuffer = decodeOutputBuffers[outIndex];
                            //Log.v(TAG, "outBuffer: " + outBuffer);

                            // AudioTrack write data
//                            audioTrack.write(chunk, decoderInfo.offset, decoderInfo.offset + decoderInfo.size);
                            if (mOnDecodeCallback != null) {
                                mOnDecodeCallback.onDecode(outBuffer, decoderInfo.offset, decoderInfo.offset + decoderInfo.size);
                            }
                            //清空buffer,否则下一次得到的还会得到同样的buffer
                            outBuffer.clear();
                            mDecoder.releaseOutputBuffer(outIndex, false);
                            break;
                    }

                    // 所有帧都解码、播放完之后退出循环
                    if ((decoderInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                        break;
                    }
                }
            }

            isPlaying = false;

            //释放MediaDecoder资源
            mDecoder.stop();
            mDecoder.release();
            mDecoder = null;

            //释放MediaExtractor资源
            mExtractor.release();
            mExtractor = null;
        }

        public void joinThread() {
            keepAlive = false;
            while (isAlive()) {
                try {
                    join(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AudioDecoder() {
        mDecodeThread = new AudioDecodeThread("AudioDecoderThread");
    }

    public void setDataSource(String audioPath) {
        mAudioPath = audioPath;
    }

    public boolean initDecoder() {
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mAudioPath);
            for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                MediaFormat mediaFormat = mExtractor.getTrackFormat(i);

                // audio mime: mp3 "audio/mpeg", aac "audio/mp4a-latm"
                codecMime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (codecMime.startsWith("audio")) {
                    mSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    mChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                    mDuration = mediaFormat.getInteger(MediaFormat.KEY_DURATION);
                    Log.i(TAG, "duration = " + mDuration);

                    mExtractor.selectTrack(i);
                    mDecoder = MediaCodec.createDecoderByType(codecMime);
                    mDecoder.configure(mediaFormat, null, null, 0);
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mDecoder == null) {
            Log.e(TAG, "create mediacodec failed.");
            return false;
        }

        mDecoder.start();
        decodeInputBuffers = mDecoder.getInputBuffers();
        decodeOutputBuffers = mDecoder.getOutputBuffers();
        decoderInfo = new MediaCodec.BufferInfo();

        return true;
    }

    public void start() {
        isPause = false;
    }

    public void pause() {
        isPause = true;
    }

    public void stop() {
        isPause = true;
    }

    public void release() {
        if (mDecodeThread != null) {
            mDecodeThread.joinThread();
            mDecodeThread = null;
        }

        if (mDecoder != null) {
            mDecoder.stop();
            mDecoder.release();
            mDecoder = null;
        }

        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }

        if (mOnDecodeCallback != null) {
            mOnDecodeCallback = null;
        }
    }

    public int getChannels() {
        return mChannels;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public void seekTo(int msec) {
        mExtractor.seekTo(msec * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
    }

    public interface OnDecodeCallback {
        /**
         * Called when the end of a media source is reached during playback.
         *
         */
        void onDecode(ByteBuffer buffer, int offset, int length);
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param callback the callback that will be run
     */
    public void setOnDecodeCallback(AudioDecoder.OnDecodeCallback callback)
    {
        mOnDecodeCallback = callback;
    }

    private OnDecodeCallback mOnDecodeCallback;
}
