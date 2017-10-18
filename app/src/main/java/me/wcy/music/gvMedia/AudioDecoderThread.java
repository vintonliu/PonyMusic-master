package me.wcy.music.gvMedia;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

//public class MainActivity extends Activity {
//
//    private static final String SAMPLE = Environment.getExternalStorageDirectory() + "/test.aac";
//    protected static AudioDecoderThread mAudioDecoder;
//    private Button btn;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        btn = (Button) findViewById(R.id.play);
//        mAudioDecoder = new AudioDecoderThread();
//        btn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    mAudioDecoder.startPlay(SAMPLE);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mAudioDecoder.stop();
//    }
//
//}


public class AudioDecoderThread {
    private static final int TIMEOUT_US = 1000;
    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;

    private boolean eosReceived;
    private int mSampleRate = 0;
    int channel = 0;
    private final String TAG = "AACPlay";
    public void startPlay(String path) throws IOException {
        eosReceived = false;
        //创建MediaExtractor对象用来解AAC封装
        mExtractor = new MediaExtractor();
        try {
            //设置需要MediaExtractor解析的文件的路径
            mExtractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }



            MediaFormat format = mExtractor.getTrackFormat(0);
            if (format == null)
            {
                Log.e(TAG,"format is null");
                return;
            }

            //判断当前帧的文件类型是否为audio
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                Log.d(TAG, "format : " + format);
                //获取当前帧的采样率
                mExtractor.selectTrack(0);
                mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                //获取当前帧的通道数
                channel = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                //音频文件长度
                long duration = format.getLong(MediaFormat.KEY_DURATION);
                Log.d(TAG,"length:"+duration/1000000);
            }


        //创建MediaCodec对象
        mDecoder = MediaCodec.createDecoderByType(mime);
        //配置MediaCodec
        mDecoder.configure(format, null, null, 0);

        if (mDecoder == null) {
            Log.e(TAG, "Can't find video info!");
            return;
        }
        //启动MediaCodec
        mDecoder.start();

        new Thread(AACDecoderAndPlayRunnable).start();
    }


    Runnable AACDecoderAndPlayRunnable = new Runnable() {

        @Override
        public void run() {
            AACDecoderAndPlay();
        }
    };


    public void AACDecoderAndPlay() {
        ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mDecoder.getOutputBuffers();

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        int buffsize = AudioTrack.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        // 创建AudioTrack对象
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffsize,
                AudioTrack.MODE_STREAM);
        //启动AudioTrack
        audioTrack.play();

        while (!eosReceived) {
            int inIndex = mDecoder.dequeueInputBuffer(TIMEOUT_US);
            if (inIndex >= 0) {
                ByteBuffer buffer = inputBuffers[inIndex];
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
                int outIndex = mDecoder.dequeueOutputBuffer(info, TIMEOUT_US);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = mDecoder.getOutputBuffers();
                        break;

                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        MediaFormat format = mDecoder.getOutputFormat();
                        Log.d(TAG, "New format " + format);
                        audioTrack.setPlaybackRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE));

                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d(TAG, "dequeueOutputBuffer timed out!");
                        break;

                    default:
                        ByteBuffer outBuffer = outputBuffers[outIndex];
                        //Log.v(TAG, "outBuffer: " + outBuffer);

                        final byte[] chunk = new byte[info.size];
                        // Read the buffer all at once
                        outBuffer.get(chunk);
                        //清空buffer,否则下一次得到的还会得到同样的buffer
                        outBuffer.clear();
                        // AudioTrack write data
                        audioTrack.write(chunk, info.offset, info.offset + info.size);
                        mDecoder.releaseOutputBuffer(outIndex, false);
                        break;
                }

                // 所有帧都解码、播放完之后退出循环
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }
            }
        }

        //释放MediaDecoder资源
        mDecoder.stop();
        mDecoder.release();
        mDecoder = null;

        //释放MediaExtractor资源
        mExtractor.release();
        mExtractor = null;

        //释放AudioTrack资源
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
    }

    public void stop() {
        eosReceived = true;
    }

}

