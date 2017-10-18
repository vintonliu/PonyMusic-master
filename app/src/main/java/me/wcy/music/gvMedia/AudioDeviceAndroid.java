/*
 *  Copyright (c) 2011 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

/*
 *  Android audio device test app
 */


package org.webrtc.voiceengine;


import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;


class AudioDeviceAndroid {
    final private static String logTag = "WebRTC AD java";

    private AudioTrack _audioTrack = null;
    private AudioRecord _audioRecord = null;
    private AudioDeviceParam _audioDeviceParam = null;

    private Context _context = null;
    private AudioManager _audioManager = null;

    private ByteBuffer _playBuffer;
    private ByteBuffer _recBuffer;
    private byte[] _tempBufPlay;
    private byte[] _tempBufRec;

    private final ReentrantLock _playLock = new ReentrantLock();
    private final ReentrantLock _recLock = new ReentrantLock();

    private boolean _doPlayInit = true;
    private boolean _doRecInit = true;
    private boolean _isRecording = false;
    private boolean _isPlaying = false;

    private int _bufferedRecSamples = 0;
    private int _bufferedPlaySamples = 0;
    private int _playPosition = 0;

    final boolean _testSwitch = false;        //the switch to write file;

    String recFileName = "/sdcard/record.pcm";
    String playFileName = "/sdcard/playout.pcm";
    //String recFileName = "/data/data/" + MainApplocation.getInstance().getApplicationContext().getPackageName() + "/"+"record.pcm";
    //String playFileName = "/data/data/" + MainApplocation.getInstance().getApplicationContext().getPackageName() + "/"+"playout.pcm";
    FileOutputStream fout_rec;
    FileOutputStream fout_play;
    Environment env;
    private boolean sdcard_flag = false;

    private String _brandString = null;
    private String _modelString = null;
    private String _boardString = null;
    private int _apiLevel;

    AudioDeviceAndroid() {
        _audioDeviceParam = AudioDeviceParam.getInstance();

        try {

            _playBuffer = ByteBuffer.allocateDirect(2 * 480); // Max 10 ms @ 48
            // kHz
            _recBuffer = ByteBuffer.allocateDirect(2 * 480); // Max 10 ms @ 48
            // kHz
        } catch (Exception e) {
            DoLog(e.getMessage());
        }

        _tempBufPlay = new byte[2 * 480];
        _tempBufRec = new byte[2 * 480];

        _brandString = getBrandString();
        _modelString = getModelString();
        _boardString = getBoardString();
        _apiLevel = getSdkApiLevel();

        if (_testSwitch) {
            try {
                //TODO by yuanwenhai 2014.06.16
                if (env.getExternalStorageState().equals(env.MEDIA_MOUNTED)) {

                    DoLog(" SD file is exits....");
                    /*fout_rec = new FileOutputStream(recFileName);
                    fout_play = new FileOutputStream(playFileName);*/
                    sdcard_flag = true;
                } else {
                    DoLogErr(" SD file don't exits....");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    private int InitRecording(int audioSource, int sampleRate) {
        // release the object
        if (_audioRecord != null) {
            DoLog("InitRecording: Release the _audioRecord before reinitializing a new one");
            _audioRecord.release();
            _audioRecord = null;
        }

        // get the minimum buffer size that can be used
        /*check input samleRating. some cellphone can't surport common sampleRate.
		  added by Rambo.Fu on 2013-10-25*/
        DoLog("InitRecording: sampleRate=" + sampleRate);
        sampleRate = getSampleRate(sampleRate, true);

        int minRecBufSize = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        // DoLog("min rec buf size is " + minRecBufSize);

        if (minRecBufSize < 2048) {
            minRecBufSize = 2048 * 2;
        }

        // double size to be more safe
        int recBufSize = minRecBufSize * 2;

        //Delete by Rambo.Fu on 2013-10-25. updata relative code for recording delay time.
        //_bufferedRecSamples = (5 * sampleRate) / 200;

        /* Get channelConfig and audioSource for MobiePhone */
        int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        channelConfig = getChannelConfig(channelConfig, true);
        audioSource = getAudioSource(audioSource);

        try {
            _audioRecord = new AudioRecord(
                    audioSource, //0 means default audio source,1 means MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT,
                    recBufSize);
        } catch (Exception e) {
            DoLog(e.getMessage());
            return -1;
        }

        // check that the audioRecord is ready to be used
        if (_audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            DoLog("rec not initialized " + sampleRate);
            DoLogErr("InitRecording: try again to new AudioTrack.");

            //try it again when first new AudioRecord fail add by zch 2013/10/23
            StopRecording();
            try {
                _audioRecord = new AudioRecord(
                        audioSource,
                        sampleRate,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        recBufSize);

            } catch (Exception e) {
                DoLog(e.getMessage());
                return -1;
            }

            if (_audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                DoLogErr("InitRecording: failed to new AudioRecord!!");
                return -1;
            }
        }

        DoLog("Successufully rec sample rate set to " + sampleRate + " _bufferedRecSamples=" + _bufferedRecSamples);

        return sampleRate;
    }

    @SuppressWarnings("unused")
    private int StartRecording() {
        if (_isPlaying == false) {
            SetAudioMode(true);
        }

        // start recording
        try {
            _audioRecord.startRecording();

        } catch (IllegalStateException e) {
            e.printStackTrace();
            return -1;
        }

        if ( sdcard_flag && _testSwitch ) {
            try {
                fout_rec = new FileOutputStream(recFileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        _isRecording = true;
        return 0;
    }

    @SuppressWarnings("unused")
    private int InitPlayback(int sampleRate) {
        DoLog("InitPlayback sampleRate=" + sampleRate);
        // get the minimum buffer size that can be used
        int minPlayBufSize = AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        // DoLog("min play buf size is " + minPlayBufSize);

        int playBufSize = minPlayBufSize;
        if (playBufSize < 6000) {
            playBufSize *= 2;
        }

        DoLog("InitPlayback: playBufSize = " + playBufSize);
        _bufferedPlaySamples = 0;
        // DoLog("play buf size is " + playBufSize);

        // release the object
        if (_audioTrack != null) {
            _audioTrack.release();
            _audioTrack = null;
        }
        
        /* Get Stream Type */
        int stream_type = AudioManager.STREAM_VOICE_CALL;
        stream_type = getStreamType(stream_type);
        DoLog("InitPlayback: stream_type = " + stream_type);

        /* modify sample rate */
        sampleRate = getSampleRate(sampleRate, false);
        DoLog("InitPlayback: sampleRate = " + sampleRate);

        /* Get channel config */
        int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        channelConfig = getChannelConfig(channelConfig, false);

        try {
            _audioTrack = new AudioTrack(
                    stream_type,
                    sampleRate,
                    channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT,
                    playBufSize, AudioTrack.MODE_STREAM);
        } catch (Exception e) {
            DoLogErr("new AudioTrack exception:");
            DoLogErr(e.getMessage());
            return -1;
        }

        // check that the audioRecord is ready to be used
        if (_audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            DoLog("init failed, state != STATE_INITIALIZED state =" + _audioTrack.getState());

            DoLogErr("InitPlayback: try again to new AudioTrack.");
            //try again if failed; --fix xiaomi2 single pass and crash bug. commit by fushuhua2013-8-8
            StopPlayback();
            _audioTrack = new AudioTrack(
                    AudioManager.STREAM_VOICE_CALL,
                    sampleRate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    playBufSize, AudioTrack.MODE_STREAM);
            if (_audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                DoLog("init failed, state != STATE_INITIALIZED state =" + _audioTrack.getState());
                return -1;
            }
        }

        // DoLog("play sample rate set to " + sampleRate);
        try {
            if (_audioManager == null && _context != null) {
                _audioManager = (AudioManager)
                        _context.getSystemService(Context.AUDIO_SERVICE);
            }
        } catch (Exception e) {
            DoLog("_context.getSystemService exception:");
            // TODO: handle exception
            DoLogErr(e.getMessage());
        }

        // Return max playout volume
        if (_audioManager == null) {
            // Don't know the max volume but still init is OK for playout,
            // so we should not return error.
            return 0;
        }
        int ret = _audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        DoLog("getStreamMaxVolume ret = " + ret);
        return ret;
    }

    @SuppressWarnings("unused")
    private int StartPlayback() {

        if (_isRecording == false) {
            SetAudioMode(true);
        }

        // start playout
        try {
            _audioTrack.play();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return -1;
        }

        if ( sdcard_flag && _testSwitch ) {
            try {
                fout_play = new FileOutputStream(playFileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        _isPlaying = true;
        return 0;
    }

    @SuppressWarnings("unused")
    private int StopRecording() {
        _recLock.lock();
        try {
            // only stop if we are recording
            if (_audioRecord.getRecordingState() ==
                    AudioRecord.RECORDSTATE_RECORDING) {
                // stop recording
                try {
                    _audioRecord.stop();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return -1;
                }
            }

            // release the object
            _audioRecord.release();
            _audioRecord = null;

        } finally {
            // Ensure we always unlock, both for success, exception or error
            // return.
            _doRecInit = true;
            _recLock.unlock();
        }

        if (_isPlaying == false) {
            SetAudioMode(false);
        }

        _isRecording = false;

        if ( sdcard_flag && _testSwitch ) {
            if ( fout_rec != null ) {
                try {
                    fout_rec.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    @SuppressWarnings("unused")
    private int StopPlayback() {
        _playLock.lock();
        try {
            // only stop if we are playing
            if (_audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                // stop playout
                try {
                    _audioTrack.stop();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    return -1;
                }

                // flush the buffers
                _audioTrack.flush();
            }

            // release the object
            _audioTrack.release();
            _audioTrack = null;

        } finally {
            // Ensure we always unlock, both for success, exception or error
            // return.
            _doPlayInit = true;
            _playLock.unlock();
        }

        if (_isRecording == false) {
            SetAudioMode(false);
        }

        _isPlaying = false;

        if ( sdcard_flag && _testSwitch ) {
            if ( fout_play != null ) {
                try {
                    fout_play.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return 0;
    }

    @SuppressWarnings("unused")
    private int PlayAudio(int lengthInBytes) {

        int bufferedSamples = 0;

        _playLock.lock();
        try {
            if (_audioTrack == null) {
                return -2; // We have probably closed down while waiting for play lock
            }

            // Set priority, only do once
            if (_doPlayInit == true) {
                try {
                    android.os.Process.setThreadPriority(
                            android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                } catch (Exception e) {
                    DoLog("Set play thread priority failed: " + e.getMessage());
                }
                _doPlayInit = false;
            }

            int written = 0;
            _playBuffer.get(_tempBufPlay);
            written = _audioTrack.write(_tempBufPlay, 0, lengthInBytes);
            _playBuffer.rewind(); // Reset the position to start of buffer

            if (_testSwitch) {
                //recording to files
                try {
                    if (sdcard_flag) {
                        fout_play.write(_tempBufPlay, 0, lengthInBytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // increase by number of written samples
            _bufferedPlaySamples += (written >> 1);

            // decrease by number of played samples
            int pos = _audioTrack.getPlaybackHeadPosition();
            if (pos < _playPosition) { // wrap or reset by driver
                _playPosition = 0; // reset
            }
            _bufferedPlaySamples -= (pos - _playPosition);
            _playPosition = pos;

            if (!_isRecording) {
                bufferedSamples = _bufferedPlaySamples;
            }

            if (written != lengthInBytes) {
                // DoLog("Could not write all data to sc (written = " + written
                // + ", length = " + lengthInBytes + ")");
                return -1;
            }

        } finally {
            // Ensure we always unlock, both for success, exception or error
            // return.
            _playLock.unlock();
        }

        return bufferedSamples;
    }

    @SuppressWarnings("unused")
    private int RecordAudio(int lengthInBytes) {
        try {
            _recLock.lock();
            if (_audioRecord == null) {
                return -2; // We have probably closed down while waiting for rec
                // lock
            }

            // Set priority, only do once
            if (_doRecInit == true) {
                try {
                    android.os.Process.setThreadPriority(
                            android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                } catch (Exception e) {
                    DoLog("Set rec thread priority failed: " + e.getMessage());
                }
                _doRecInit = false;
            }

            int readBytes = 0;
            _recBuffer.rewind(); // Reset the position to start of buffer
            readBytes = _audioRecord.read(_tempBufRec, 0, lengthInBytes);
            //DoLog("read " + readBytes + "from SC");
            _recBuffer.put(_tempBufRec);

            if (_testSwitch) {
                //recording to files
                try {
                    //fout.write(_tempBufRec);
                    if (sdcard_flag) {
                        fout_rec.write(_tempBufRec, 0, lengthInBytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (readBytes != lengthInBytes) {
                // DoLog("Could not read all data from sc (read = " + readBytes
                // + ", length = " + lengthInBytes + ")");
                return -1;
            }

        } catch (Exception e) {
            DoLogErr("RecordAudio try failed: " + e.getMessage());

        } finally {
            // Ensure we always unlock, both for success, exception or error
            // return.
            _recLock.unlock();
        }

        return (_bufferedPlaySamples);
    }

    @SuppressWarnings("unused")
    private int SetPlayoutSpeaker(boolean loudspeakerOn) {
        // create audio manager if needed
        DoLog("setPlayoutSpeaker is " + loudspeakerOn);

        if (_audioManager == null && _context != null) {
            try {
                _audioManager = (AudioManager)
                        _context.getSystemService(Context.AUDIO_SERVICE);
            } catch (Exception e) {
                // TODO: handle exception
                DoLogErr("SetPlayoutSpeaker ==== trace begin ====");
                DoLogErr(e.getMessage());
                DoLogErr(e.toString());

                DoLogErr("SetPlayoutSpeaker ===== trace end =====");
            }
        }

        if (_audioManager == null) {
            DoLogErr("Could not change audio routing - no audio manager");
            return -1;
        }

        DoLog("SetPlayoutSpeaker: apiLevel = " + _apiLevel);
        DoLog("SetPlayoutSpeaker: bandString = " + _brandString + " modelString = " + _modelString);

        if ( _audioDeviceParam.getDynamicPolicyEnable() ) {
            int mode = getSpeakerMode(loudspeakerOn);
            _audioManager.setMode(mode);
            _audioManager.setSpeakerphoneOn(loudspeakerOn);

            if (_audioManager.getMode() != mode) {
                DoLogErr("Could not set audio mode (" + mode + ") for current device");
            } else {
                DoLogErr("set audio mode for current device success");
            }
        } else {
            if ((3 == _apiLevel) || (4 == _apiLevel)) {
                // 1.5 and 1.6 devices
                if (loudspeakerOn) {
                    // route audio to back speaker
                    _audioManager.setMode(AudioManager.MODE_NORMAL);
                } else {
                    // route audio to earpiece
                    _audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
            } else {
                // 2.x devices
                if ((_brandString.equalsIgnoreCase("Samsung")) &&
                        ((5 == _apiLevel) || (6 == _apiLevel) ||
                                (7 == _apiLevel))) {
                    // Samsung 2.0, 2.0.1 and 2.1 devices
                    if (loudspeakerOn) {
                        // route audio to back speaker
                        _audioManager.setMode(AudioManager.MODE_IN_CALL);
                        _audioManager.setSpeakerphoneOn(loudspeakerOn);
                        DoLogErr("Samsung and Samsung 2.1 and down devices:route audio to  back speaker success");
                    } else {
                        // route audio to earpiece
                        _audioManager.setSpeakerphoneOn(loudspeakerOn);
                        _audioManager.setMode(AudioManager.MODE_NORMAL);
                        DoLogErr("Samsung and Samsung 2.1 and down devices:route audio to  earpiece success");
                    }
                } else {

                    if (_brandString.equals("yusu")
                            || _brandString.equals("yusuH701")
                            || _brandString.equals("yusuA2")
                            || _brandString.equals("qcom")
                            || _brandString.equals("motoME525")) {

                        _audioManager.setMode(AudioManager.MODE_IN_CALL);
                        _audioManager.setSpeakerphoneOn(loudspeakerOn);
                    } else if (_brandString.equals("Huawei") && (_modelString.equals("HUAWEIP6-C00"))) {
                        _audioManager.setMode(AudioManager.MODE_NORMAL);
                        _audioManager.setSpeakerphoneOn(loudspeakerOn);
                    } else if (_brandString.equals("Lenovo")
                            && ((_modelString.equals("LenovoA788t"))
                            || (_modelString.equals("LenovoA760")))) {

                        _audioManager.setMode(AudioManager.MODE_IN_CALL);
                        _audioManager.setSpeakerphoneOn(loudspeakerOn);
                    } else if (_brandString.equals("Huawei") && (_modelString.equals("U9200"))) {
                        if (loudspeakerOn) {

                            _audioManager.setMode(AudioManager.MODE_NORMAL);
                            _audioManager.setSpeakerphoneOn(loudspeakerOn);
                        } else {

                            _audioManager.setSpeakerphoneOn(loudspeakerOn);
                            SetAudioMode(true);
                        }
                    } else if (_modelString.equalsIgnoreCase("MI2")
                            || _modelString.equalsIgnoreCase("MI2S")) {

                        SetAudioMode(true);
                        _audioManager.setSpeakerphoneOn(loudspeakerOn);
                    } else if ((_brandString.equalsIgnoreCase("Nokia"))
                            && (_modelString.equalsIgnoreCase("Nokia_X"))) {

                        SetAudioMode(true);
                        _audioManager.setSpeakerphoneOn(loudspeakerOn);
                    } else if (_brandString.equalsIgnoreCase("ErenEben")) {
                        if (_modelString.equalsIgnoreCase("EBENM1")) {
                            _audioManager.setSpeakerphoneOn(loudspeakerOn);
                            SetAudioMode(true);
                        }
                    } else if (_modelString.equalsIgnoreCase("HUAWEIC8815")) {
                        SetAudioMode(true);
                        _audioManager.setSpeakerphoneOn(loudspeakerOn);
                    } else {
                        // Non-Samsung and Samsung 2.2 and up devices
                        DoLogErr("Non-Samsung and Samsung 2.2 and up devices:route audio to  back speaker? "
                                + loudspeakerOn + " success. " + " mode = " + _audioManager.getMode());

                        _audioManager.setSpeakerphoneOn(loudspeakerOn);
                    }
                }
            }
        }

        return 0;
    }

    @SuppressWarnings("unused")
    private int SetPlayoutVolume(int level) {

        // create audio manager if needed
        if (_audioManager == null && _context != null) {
            try {
                _audioManager = (AudioManager)
                        _context.getSystemService(Context.AUDIO_SERVICE);
            } catch (Exception e) {
                // TODO: handle exception
                DoLogErr(e.toString());
            }
        }

        int retVal = -1;

        if (_audioManager != null) {
            _audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, level, 0);
            retVal = 0;
        }

        return retVal;
    }

    @SuppressWarnings("unused")
    private int GetPlayoutVolume() {

        // create audio manager if needed
        if (_audioManager == null && _context != null) {
            try {
                _audioManager = (AudioManager)
                        _context.getSystemService(Context.AUDIO_SERVICE);
            } catch (Exception e) {
                // TODO: handle exception
                DoLogErr(e.toString());
            }

        }

        int level = -1;

        if (_audioManager != null) {
            level = _audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        }

        return level;
    }

    /**
     * Adaptive audio mode for phone
     *
     * @param startCall true is on call, else is wait state
     */
    private void SetAudioMode(boolean startCall) {

        if (_audioManager == null && _context != null) {
            _audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
        }

        if (_audioManager == null) {
            DoLogErr("Could not set audio mode - no audio manager");
            return;
        }

        int mode = _audioManager.getMode();
        DoLog("SetAudioMode: current mode = " + mode + " startCall = " + startCall);

        // ***IMPORTANT*** When the API level for honeycomb (H) has been
        // decided,
        // the condition should be changed to include API level 8 to H-1.
        DoLog("SetAudioMode: brandString = " + _brandString + " modelString = " + _modelString);
        DoLog("SetAudioMode: apiLevel = " + _apiLevel);

        if ( _audioDeviceParam.getDynamicPolicyEnable() ) {
            mode = startCall ? getCallMode() : AudioManager.MODE_NORMAL;
        } else {
            if (_brandString.equalsIgnoreCase("Samsung")) {  // for Samsung
                if ((_apiLevel == 8)) {
                    // Set Samsung specific VoIP mode for 2.2 devices
                    mode = (startCall ? 4 : AudioManager.MODE_NORMAL); // 4 is VoIP mode??not find in api
                }
            } else if (_brandString.equals("Lenovo")) {  // Lenovo
                if ((_modelString.equals("LenovoA788t"))) {

                    mode = AudioManager.MODE_IN_CALL;
                } else if ((_modelString.equalsIgnoreCase("LenovoS850e"))
                        || (_modelString.equalsIgnoreCase("LenovoA60"))
                        || (_modelString.equalsIgnoreCase("LenovoA780"))
                        || (_modelString.equalsIgnoreCase("LenovoA820e"))) {

                    mode = (startCall ? AudioManager.MODE_IN_CALL : AudioManager.MODE_NORMAL);
                }
            } else if (_brandString.equalsIgnoreCase("Huawei")) { // huawei
                if (_modelString.equals("HUAWEIP6-C00")) {
                    mode = (startCall ? AudioManager.MODE_NORMAL : AudioManager.MODE_IN_CALL);
                } else if ( _modelString.equalsIgnoreCase("HUAWEIC8815")) {
                    mode = AudioManager.MODE_NORMAL;
                } else if (!_modelString.equalsIgnoreCase("HUAWEIY220T")
                        && !_modelString.equalsIgnoreCase("HUAWEIT8600")
                        && !_modelString.equalsIgnoreCase("HUAWEIY310-T10")) {

                    mode = (startCall ? AudioManager.MODE_IN_CALL : AudioManager.MODE_NORMAL);
                }
            } else if (_brandString.equalsIgnoreCase("ZTE")) {  // zte
                if (!_modelString.equalsIgnoreCase("ZTEU880E")
                        && !_modelString.equalsIgnoreCase("ZTEV985")
                        && !_modelString.equalsIgnoreCase("ZTEU950")
                        && !_modelString.equalsIgnoreCase("ZTE-TU880")
                        && !_modelString.equalsIgnoreCase("ZTE-TU960s")
                        && !_modelString.equalsIgnoreCase("ZTEU793")) {

                    mode = (startCall ? AudioManager.MODE_IN_CALL : AudioManager.MODE_NORMAL);
                }
            } else if (_brandString.equalsIgnoreCase("motorola")) { // motorola
                if ((_modelString.equals("MOT-XT788"))) {

                    mode = (startCall ? AudioManager.MODE_IN_CALL : AudioManager.MODE_NORMAL);
                }
            } else if (_brandString.equalsIgnoreCase("Coolpad")) { // coolpad
                if (_modelString.equals("Coolpad5950") || _modelString.equals("Coolpad5891")) {

                    mode = (startCall ? 4 : AudioManager.MODE_NORMAL);
                } else if ((_modelString.equalsIgnoreCase("Coolpad5890"))
                        || (_modelString.equalsIgnoreCase("7260"))) {

                    mode = (startCall ? AudioManager.MODE_IN_CALL : AudioManager.MODE_NORMAL);
                }
            } else if (_brandString.equalsIgnoreCase("xiaomi")) { // xiaomi
                if (_modelString.equals("MI1S") || _modelString.equals("HM1SC")) {

                    mode = (startCall ? AudioManager.MODE_IN_COMMUNICATION : AudioManager.MODE_NORMAL);
                    boolean speakon = _audioManager.isSpeakerphoneOn();
                    speakon = false;
                    _audioManager.setSpeakerphoneOn(speakon);
                } else if (_modelString.equals("MI2S") || _modelString.equals("MI2")) {

                    mode = AudioManager.MODE_NORMAL;
                }
            } else if (_brandString.equalsIgnoreCase("Sony")) { // Sony
                if ((_modelString.equals("M35c"))) {

                    mode = (startCall ? AudioManager.MODE_IN_CALL : AudioManager.MODE_NORMAL);
                }
            } else if (_brandString.equalsIgnoreCase("Nokia")) {  // Nokia
                if ((_modelString.equalsIgnoreCase("Nokia_X"))) {

                    mode = (startCall ? AudioManager.MODE_IN_COMMUNICATION : AudioManager.MODE_NORMAL);
                }
            } else if (_brandString.equalsIgnoreCase("ErenEben")) {
                if (_modelString.equalsIgnoreCase("EBENM1")) {
                    mode = (startCall ? AudioManager.MODE_IN_COMMUNICATION : AudioManager.MODE_NORMAL);
                }
            } else if ( (_brandString.equalsIgnoreCase("yusu") // others
                    || _brandString.equalsIgnoreCase("yusuH701")
                    || _brandString.equalsIgnoreCase("yusuA2")
                    || _brandString.equalsIgnoreCase("qcom")
                    || _brandString.equalsIgnoreCase("motoME525")
                    || _brandString.equalsIgnoreCase("lge")
                    || _brandString.equalsIgnoreCase("SEMC")
                    || _modelString.equalsIgnoreCase("HTCA510e")
                    || _brandString.equalsIgnoreCase("ChanghongV10")
                    || _modelString.equalsIgnoreCase("MT788")
                    || _modelString.equalsIgnoreCase("MI-ONEPlus")) ) {
                mode = (startCall ? AudioManager.MODE_IN_CALL : AudioManager.MODE_NORMAL);
            }
        }

        _audioManager.setMode(mode);
        if (_audioManager.getMode() != mode) {
            DoLogErr("Could not set audio mode (" + mode + ") for current device");
        }

        DoLogErr("SetAudioMode: Success. mode = " + _audioManager.getMode());
    }

    /**
     * Get audio mode for calling
     * @return  audio mode for calling
     */
    private int getCallMode() {
        if (_audioManager == null && _context != null) {
            try {
                _audioManager = (AudioManager)
                        _context.getSystemService(Context.AUDIO_SERVICE);
            } catch (Exception e) {
                // TODO: handle exception
                DoLogErr("getSpeakerMode ==== trace begin ====");
                DoLogErr(e.getMessage());
                DoLogErr(e.toString());

                DoLogErr("getSpeakerMode ===== trace end =====");
            }
        }

        if (_audioManager == null) {
            DoLogErr("Could not change audio routing - no audio manager");
            return -1;
        }

        if ( _audioDeviceParam == null ) {
            _audioDeviceParam = AudioDeviceParam.getInstance();
        }

        int mode = _audioDeviceParam.getCallMode();

        switch ( mode ) {
            case 0:
                mode = AudioManager.MODE_NORMAL;
                break;

            case 1:
                mode = AudioManager.MODE_IN_CALL;
                break;

            case 2:
                mode = AudioManager.MODE_IN_COMMUNICATION;
                break;

            case 3:
                mode = 4;
                break;

            default:
                DoLog("getCallMode: default!!!");
                mode = _audioManager.getMode();
                break;
        }

        DoLog("getCallMode: mode = " + mode);
        return mode;
    }

    /**
     * Get speaker audio mode
     * @param bSpeakerOn true for get speaker mode, false for get earpiece mode
     * @return speaker audio mode
     */
    private int getSpeakerMode( boolean bSpeakerOn ) {
        if (_audioManager == null && _context != null) {
            try {
                _audioManager = (AudioManager)
                        _context.getSystemService(Context.AUDIO_SERVICE);
            } catch (Exception e) {
                // TODO: handle exception
                DoLogErr("getSpeakerMode ==== trace begin ====");
                DoLogErr(e.getMessage());
                DoLogErr(e.toString());

                DoLogErr("getSpeakerMode ===== trace end =====");
            }
        }

        if (_audioManager == null) {
            DoLogErr("Could not change audio routing - no audio manager");
            return -1;
        }

        if ( _audioDeviceParam == null ) {
            _audioDeviceParam = AudioDeviceParam.getInstance();
        }

        int mode = bSpeakerOn ? _audioDeviceParam.getSpeakerMode() : _audioDeviceParam.getEarpieceMode();
        switch ( mode ) {
            case 0:
                mode = AudioManager.MODE_NORMAL;
                break;

            case 1:
                mode = AudioManager.MODE_IN_CALL;
                break;

            case 2:
                mode = AudioManager.MODE_IN_COMMUNICATION;
                break;

            case 3:
                mode = 4;
                break;

            default:
                DoLog("getSpeakerMode: default!!!");
                mode = _audioManager.getMode();
                break;
        }

        DoLog("getSpeakerMode: bSpeakerOn = " + bSpeakerOn + " mode = " + mode);
        return mode;
    }

    /**
     * Adaptive sample rate for phone
     *
     * @param sampleRate
     * @return sampleRate suitable for phone
     */
    private int getSampleRate(int sampleRate, boolean bRecord) {
        if ( _audioDeviceParam == null ) {
            _audioDeviceParam = AudioDeviceParam.getInstance();
        }

        int newSampleRate = sampleRate;
        if ( _audioDeviceParam.getDynamicPolicyEnable() ) {
            newSampleRate = bRecord ? _audioDeviceParam.getRecordSampleRate() : _audioDeviceParam.getPlaySampleRate();

            DoLog("GetSampleRate: brandString = " + _brandString + " modelString = " + _modelString);
            switch (newSampleRate) {
                case 0:
                    newSampleRate = 8000;
                    break;

                case 1:
                    newSampleRate = 16000;
                    break;

                case 2:
                    newSampleRate = 44100;
                    break;

                default:
                    newSampleRate = sampleRate;
                    break;
            }
        } else {
            /* for Audio Record */
            if ( bRecord ) {
                if (_brandString.equalsIgnoreCase("Huawei")) {
                    if (_modelString.equals("HUAWEIG520-0000")
                            || _modelString.equals("HUAWEIC8813Q")
                            || _modelString.equals("HUAWEIG610-C00")
                            || _modelString.equals("HUAWEIC8815")) {

                        newSampleRate = 16000;
                    }
                } else if (_brandString.equalsIgnoreCase("Nokia")) {
                    if ((_modelString.equals("Nokia_X"))) {

                        newSampleRate = 16000;
                    }
                } else if (_brandString.equals("Lenovo")) {
                    if ((_modelString.equals("LenovoA788t"))) {

                        newSampleRate = 8000;
                    } else if ((_modelString.equals("LenovoA760"))) {

                        newSampleRate = 16000;
                    }
                } else if (_brandString.equalsIgnoreCase("Coolpad")) {
                    if ((_modelString.equals("Coolpad5890"))
                            || (_modelString.equals("Coolpad5891"))
                            || (_modelString.equals("Coolpad5950"))) {

                        newSampleRate = 16000;
                    }
                }
            }
        }

        DoLog("SampleRate changed " + "old = " + sampleRate + "  new = " + newSampleRate);
        return (newSampleRate);
    }

    /**
     * Adaptive channelConfig for difference MobiePhone
     *
     * @param channelConfig
     * @return channelConfig suitable for phone
     */
    private int getChannelConfig(int channelConfig, boolean bRecord) {
        if ( _audioDeviceParam == null ) {
            _audioDeviceParam = AudioDeviceParam.getInstance();
        }

        int newChannelConfig = channelConfig;
        if ( _audioDeviceParam.getDynamicPolicyEnable() ) {
            newChannelConfig = bRecord ? _audioDeviceParam.getRecordChannel() : _audioDeviceParam.getPlayChannel();

            switch (newChannelConfig) {
                case 0:
                    newChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
                    break;

                case 1:
                    newChannelConfig = bRecord ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_OUT_MONO;
                    break;

                default:
                    newChannelConfig = channelConfig;
                    break;
            }
        } else {
            /* for AudioRecord */
            if ( bRecord ) {
                if (_brandString.equals("Lenovo")) {
                    if ((_modelString.equals("LenovoA788t"))
                            || (_modelString.equals("LenovoA820e"))) {

                        newChannelConfig = AudioFormat.CHANNEL_IN_MONO;
                    }
                } else if ((_modelString.equals("ZTEU793")) //2014.06.19 add by charlie yuan
                        || (_modelString.equals("ZTEU950"))) {

                    newChannelConfig = AudioFormat.CHANNEL_IN_MONO;
                }
            }
        }

        DoLog("GetChannelConfig: changed. old = " + channelConfig + " new = " + newChannelConfig);
        return newChannelConfig;
    }

    /**
     * Adaptive audioSource for difference MobiePhone
     *
     * @param audioSource default value
     * @return audioSource suitable for phone
     */
    private int getAudioSource(int audioSource) {
        if ( _audioDeviceParam == null ) {
            _audioDeviceParam = AudioDeviceParam.getInstance();
        }

        int newAudioSource = audioSource;
        if ( _audioDeviceParam.getDynamicPolicyEnable() ) {
            newAudioSource = _audioDeviceParam.getRecordSource();

            switch (newAudioSource) {
                case 0:
                    newAudioSource = AudioSource.DEFAULT;
                    break;

                case 1:
                    newAudioSource = AudioSource.MIC;
                    break;

                case 2:
                    newAudioSource = AudioSource.VOICE_CALL;
                    break;

                case 3:
                    newAudioSource = AudioSource.VOICE_COMMUNICATION;
                    break;

                default:
                    newAudioSource = audioSource;
                    break;
            }
        } else {
            //brand13310modelHUAWEIY310-T10
            if (((_modelString.equals("MI3W"))) ||
                    ((_modelString.equals("HUAWEIY310-T10")))) {

                newAudioSource = MediaRecorder.AudioSource.VOICE_CALL;
            } else if (_brandString.equals("Lenovo")) {
                if ((_modelString.equals("LenovoA788t"))) {

                    newAudioSource = MediaRecorder.AudioSource.VOICE_CALL;
                } else if ((_modelString.equals("LenovoA760"))) {

                    //board = 7x27modelLenovoA760
                    newAudioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
                }
            }
        }

        DoLog("getAudioSource: changed old = " + audioSource + " new = " + newAudioSource);
        return newAudioSource;
    }

    /**
     * Adaptive streamtype for difference MobiePhone
     *
     * @param streamType default value
     * @return streamtype suitable for phone
     */
    private int getStreamType( int streamType ) {
        if ( _audioDeviceParam == null ) {
            _audioDeviceParam = AudioDeviceParam.getInstance();
        }
        int newStreamType = streamType;

        if ( _audioDeviceParam.getDynamicPolicyEnable() ) {
            newStreamType = _audioDeviceParam.getPlayStreamType();

            switch (newStreamType) {
                case 0:
                    newStreamType = AudioManager.STREAM_SYSTEM;
                    break;

                case 1:
                    newStreamType = AudioManager.STREAM_VOICE_CALL;
                    break;

                default:
                    newStreamType = streamType;
                    break;
            }
        } else {
            //for motorala XT788 add by andy_zeng 2013/8/16
            if (_brandString.equalsIgnoreCase("motorola")) {  //for motorola
                if ((_modelString.equals("MOT-XT788"))) {
                    newStreamType = AudioManager.STREAM_SYSTEM; //AudioManager.STREAM_DTMF,ok
                }
            } else if (_brandString.equalsIgnoreCase("Huawei")) {  //for huawei
                if ((_modelString.equals("HUAWEIC8813Q"))
                        || (_modelString.equals("HUAWEIY300-0000"))
                        || (_modelString.equals("HUAWEIG520-0000"))
                        || (_modelString.equals("HUAWEIC8813"))) {
                    newStreamType = AudioManager.STREAM_SYSTEM;
                }
            } else if (_brandString.equalsIgnoreCase("Lenovo")) {  //for Lenovo
                if ((_modelString.equals("LenovoA788t"))
                        || (_modelString.equals("LenovoA820e"))) {
                    newStreamType = AudioManager.STREAM_SYSTEM;
                }
            } else if (_brandString.equalsIgnoreCase("innos_smartphone")) {  //for innos
                newStreamType = AudioManager.STREAM_SYSTEM;
            }
        }

        DoLog("GetStreamType: changed. old = " + streamType + " new = " + newStreamType);
        return newStreamType;
    }

    /**
     * Get Brand string of MobiePhone
     *
     * @return current brand string
     */
    private String getBrandString() {
        return android.os.Build.BRAND;
    }

    /**
     * Get Model string of MobiePhone
     *
     * @return current model string
     */
    private String getModelString() {
        String modelString = "";
        if (Build.MODEL != null)
            modelString = Build.MODEL.replaceAll(" ", "");
        return modelString;
    }

    /**
     * Get sdk api level
     *
     * @return current sdk level
     */
    private int getSdkApiLevel() {
        return Integer.parseInt(android.os.Build.VERSION.SDK);
    }

    /**
     * Get Board String of MobiePhone
     *
     * @return current board String
     */
    private String getBoardString() {
        String boardString = "";
        if (android.os.Build.BOARD != null) {
            boardString = android.os.Build.BOARD;
        }

        return boardString;
    }

    private void DoLog(String msg) {
        if (msg != null) {
            Log.i(logTag, msg);
        }
    }

    private void DoLogErr(String msg) {
        if (msg != null) {

            Log.e(logTag, msg);
        }

    }
}
