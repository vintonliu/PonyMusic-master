package me.wcy.music.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;

import com.gvmedia.apollo.GvApolloAudioConfig;
import com.gvmedia.apollo.GvApolloEnum;
import com.gvmedia.apollo.GvApolloManager;
import com.gvmedia.apollo.GvApolloSoundEffectConfig;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import me.wcy.music.application.AppCache;
import me.wcy.music.application.Notifier;
import me.wcy.music.constants.Actions;
import me.wcy.music.enums.PlayModeEnum;
import me.wcy.music.gvMedia.AudioDecoder;
import me.wcy.music.gvMedia.AudioPlayer;
import me.wcy.music.model.Music;
import me.wcy.music.receiver.NoisyAudioStreamReceiver;
import me.wcy.music.utils.MusicUtils;
import me.wcy.music.utils.ParseUtils;
import me.wcy.music.utils.Preferences;

/**
 * 音乐播放后台服务
 * Created by wcy on 2015/11/27.
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener,
        AudioPlayer.OnCompletionListener
{
    private static final String TAG = "PlayService";
    private static final long TIME_UPDATE = 100L;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSE = 3;

    private final List<Music> mMusicList = AppCache.getMusicList();
    private final IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final NoisyAudioStreamReceiver mNoisyReceiver = new NoisyAudioStreamReceiver();
    private final Handler mHandler = new Handler();
    private MediaPlayer mPlayer = new MediaPlayer();
    private AudioManager mAudioManager;
    private OnPlayerEventListener mListener;
    // 正在播放的歌曲[本地|网络]
    private Music mPlayingMusic;
    // 正在播放的本地歌曲的序号
    private int mPlayingPosition;
    private long quitTimerRemain;
    private int mPlayState = STATE_IDLE;

    private AudioPlayer mAudioPlayer = new AudioPlayer();
    private static boolean isMP = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: " + getClass().getSimpleName());
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mPlayer.setOnCompletionListener(this);
        Notifier.init(this);

        mAudioPlayer.setOnCompletionListener(this);

//        GvApolloAudioConfig audioConfig = new GvApolloAudioConfig(48000, 2);
//        GvApolloManager.getInstance().GvSetSetting(GvApolloEnum.SETTING_AUDIO_ID, audioConfig);
//        GvApolloManager.getInstance().GvInit();
//        GvApolloManager.getInstance().GvGetSetting(GvApolloEnum.SETTING_AUDIO_ID, audioConfig);
//        Log.i("SplashActivity", "sampleRate = " + audioConfig.mSampleRate + " channels = " + audioConfig.mChannels);
//
//        int maxLen = GvApolloManager.getInstance().GvGetMaxBlockLength();
//        short[][] datainput = new short[GvApolloEnum.kSrs2_0][maxLen];
//        short[][] dataoutput = new short[GvApolloEnum.kSrs2_0][maxLen];
//        for (int row = 0; row < GvApolloEnum.kSrs2_0; row++) {
//            for (int col = 0; col < GvApolloEnum.kMaxBlockLength; col++) {
//                datainput[row][col] = (short)((row + 1) * col);
//            }
//        }
//        GvApolloManager.getInstance().GvProcess(datainput, dataoutput, GvApolloEnum.kMaxBlockLength);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public static void startCommand(Context context, String action) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Actions.ACTION_MEDIA_PLAY_PAUSE:
                    playPause();
                    break;
                case Actions.ACTION_MEDIA_NEXT:
                    next();
                    break;
                case Actions.ACTION_MEDIA_PREVIOUS:
                    prev();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    /**
     * 扫描音乐
     */
    public void updateMusicList(final EventCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                MusicUtils.scanMusic(PlayService.this, mMusicList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (!mMusicList.isEmpty()) {
                    updatePlayingPosition();
                    mPlayingMusic = mMusicList.get(mPlayingPosition);
                }

                if (mListener != null) {
                    mListener.onMusicListUpdate();
                }

                if (callback != null) {
                    callback.onEvent(null);
                }
            }
        }.execute();
    }

    public void updateSoundEffect() {
        int effectId = ParseUtils.parseInt(Preferences.getSoundEffect());
        GvApolloManager.getInstance().GvSetSetting(GvApolloEnum.SETTING_SOUND_EFFECT_ID,
                new GvApolloSoundEffectConfig(effectId));
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    @Override
    public void onCompletion() {
        next();
    }

    public OnPlayerEventListener getOnPlayEventListener() {
        return mListener;
    }

    public void setOnPlayEventListener(OnPlayerEventListener listener) {
        mListener = listener;
    }

    public void play(int position) {
        Log.i(TAG, "play() position = " + position);
        if (mMusicList.isEmpty()) {
            return;
        }

        if (position < 0) {
            position = mMusicList.size() - 1;
        } else if (position >= mMusicList.size()) {
            position = 0;
        }

        mPlayingPosition = position;
        Music music = mMusicList.get(mPlayingPosition);
        Preferences.saveCurrentSongId(music.getId());
        play(music);
    }

    public void play(Music music) {
        Log.i(TAG, "play(music)");
        mPlayingMusic = music;
        try {
            mPlayState = STATE_PREPARING;

            if (isMP) {
                mPlayer.reset();
                mPlayer.setDataSource(music.getPath());
                mPlayer.prepareAsync();
                mPlayer.setOnPreparedListener(mPreparedListener);
                mPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            } else {
                mAudioPlayer.reset();
                mAudioPlayer.setDataSource(music.getPath());
                mAudioPlayer.setOnPreparedListener(mAPrepareListener);
                mAudioPlayer.prepareAsync();
            }

//            Log.i(TAG, "music duration = " + music.getDuration());

            if (mListener != null) {
                mListener.onChange(music);
            }
            Notifier.showPlay(music);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (isPreparing()) {
                start();
            }
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (mListener != null) {
                mListener.onBufferingUpdate(percent);
            }
        }
    };

    private AudioPlayer.OnPreparedListener mAPrepareListener = new AudioPlayer.OnPreparedListener() {
        @Override
        public void onPrepared() {
            Log.i(TAG, "AudioPlayer.OnPreparedListener");
            if (isPreparing()) {
                start();
            }
        }
    };

    public void playPause() {
        Log.i(TAG, "playPause()");
        if (isPreparing()) {
            stop();
        } else if (isPlaying()) {
            pause();
        } else if (isPausing()) {
            resume();
        } else {
            play(getPlayingPosition());
        }
    }

    private boolean start() {
        boolean isPlaying;
        Log.i(TAG, "start()");
        if (isMP) {
            mPlayer.start();
            isPlaying =  mPlayer.isPlaying();
        } else {
            mAudioPlayer.start();
            isPlaying = mAudioPlayer.isPlaying();
        }

        if (isPlaying) {
            mPlayState = STATE_PLAYING;
            mHandler.post(mPublishRunnable);
            Notifier.showPlay(mPlayingMusic);
            mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            registerReceiver(mNoisyReceiver, mNoisyFilter);
        }

        return isPlaying;
    }

    private void pause() {
        Log.i(TAG, "pause()");
        if (!isPlaying()) {
            return;
        }

        if (isMP) {
            mPlayer.pause();
        } else {
            mAudioPlayer.pause();
        }

        mPlayState = STATE_PAUSE;
        mHandler.removeCallbacks(mPublishRunnable);
        Notifier.showPause(mPlayingMusic);
        mAudioManager.abandonAudioFocus(this);
        unregisterReceiver(mNoisyReceiver);
        if (mListener != null) {
            mListener.onPlayerPause();
        }
    }

    public void stop() {
        if (isIdle()) {
            return;
        }

        pause();
        if (isMP) {
            mPlayer.reset();
        } else {
            mAudioPlayer.reset();
        }
        mPlayState = STATE_IDLE;
    }

    private void resume() {
        Log.i(TAG, "resume()");
        if (!isPausing()) {
            return;
        }

        if (start()) {
            if (mListener != null) {
                mListener.onPlayerResume();
            }
        }
    }

    public void next() {
        if (mMusicList.isEmpty()) {
            return;
        }

        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                mPlayingPosition = new Random().nextInt(mMusicList.size());
                play(mPlayingPosition);
                break;
            case SINGLE:
                play(mPlayingPosition);
                break;
            case LOOP:
            default:
                play(mPlayingPosition + 1);
                break;
        }
    }

    public void prev() {
        if (mMusicList.isEmpty()) {
            return;
        }

        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                mPlayingPosition = new Random().nextInt(mMusicList.size());
                play(mPlayingPosition);
                break;
            case SINGLE:
                play(mPlayingPosition);
                break;
            case LOOP:
            default:
                play(mPlayingPosition - 1);
                break;
        }
    }

    /**
     * 跳转到指定的时间位置
     *
     * @param msec 时间
     */
    public void seekTo(int msec) {
        if (isPlaying() || isPausing()) {
            if (isMP) {
                mPlayer.seekTo(msec);
            } else {
                mAudioPlayer.seekTo(msec);
            }

            if (mListener != null) {
                mListener.onPublish(msec);
            }
        }
    }

    public boolean isPlaying() {
        return mPlayState == STATE_PLAYING;
    }

    public boolean isPausing() {
        return mPlayState == STATE_PAUSE;
    }

    public boolean isPreparing() {
        return mPlayState == STATE_PREPARING;
    }

    public boolean isIdle() {
        return mPlayState == STATE_IDLE;
    }

    public static boolean isMP() {
        return isMP;
    }

    /**
     * 获取正在播放的本地歌曲的序号
     */
    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    /**
     * 获取正在播放的歌曲[本地|网络]
     */
    public Music getPlayingMusic() {
        return mPlayingMusic;
    }

    /**
     * 删除或下载歌曲后刷新正在播放的本地歌曲的序号
     */
    public void updatePlayingPosition() {
        int position = 0;
        long id = Preferences.getCurrentSongId();
        for (int i = 0; i < mMusicList.size(); i++) {
            if (mMusicList.get(i).getId() == id) {
                position = i;
                break;
            }
        }
        mPlayingPosition = position;
        Preferences.saveCurrentSongId(mMusicList.get(mPlayingPosition).getId());
    }

    public int getAudioSessionId() {
        return mPlayer.getAudioSessionId();
    }

    private Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying() && mListener != null) {
                if (isMP) {
                    mListener.onPublish(mPlayer.getCurrentPosition());
                } else {
                    mListener.onPublish(mAudioPlayer.getCurrentPosition());
                }
            }
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                pause();
                break;
        }
    }

    public void startQuitTimer(long milli) {
        stopQuitTimer();
        if (milli > 0) {
            quitTimerRemain = milli + DateUtils.SECOND_IN_MILLIS;
            mHandler.post(mQuitRunnable);
        } else {
            quitTimerRemain = 0;
            if (mListener != null) {
                mListener.onTimer(quitTimerRemain);
            }
        }
    }

    private void stopQuitTimer() {
        mHandler.removeCallbacks(mQuitRunnable);
    }

    private Runnable mQuitRunnable = new Runnable() {
        @Override
        public void run() {
            quitTimerRemain -= DateUtils.SECOND_IN_MILLIS;
            if (quitTimerRemain > 0) {
                if (mListener != null) {
                    mListener.onTimer(quitTimerRemain);
                }
                mHandler.postDelayed(this, DateUtils.SECOND_IN_MILLIS);
            } else {
                AppCache.clearStack();
                quit();
            }
        }
    };

    @Override
    public void onDestroy() {
        GvApolloManager.getInstance().GvClose();
        AppCache.setPlayService(null);
        super.onDestroy();
        Log.i(TAG, "onDestroy: " + getClass().getSimpleName());
    }

    public void quit() {
        stop();
        stopQuitTimer();
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
        mAudioPlayer.release();
        mAudioPlayer = null;
        Notifier.cancelAll();
        AppCache.setPlayService(null);
        stopSelf();
    }

    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }
}
