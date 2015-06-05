package com.gfjalar.fiftytwo;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

import android.os.Process;


public class AudioRecorder {
    private short[] mBuffer;

    private AudioRecord mRecorder;
    private AudioProcessor mProcessor;
    private AudioRecorderThread mRecorderThread;

    private int mBufferSize;
    private int mChannels = AudioFormat.CHANNEL_IN_MONO;
    private int mBitsPerSample = AudioFormat.ENCODING_PCM_8BIT;
    private int mSampleRate = 44100;
    private int mAudioSource = AudioSource.MIC;

    public AudioRecorder(int channels, int bitsPerSample, int bufferSize, AudioProcessor processor) {
        mProcessor = processor;
        mBufferSize = bufferSize;
        if (channels == 2) {
            mChannels = AudioFormat.CHANNEL_IN_STEREO;
        }
        if (bitsPerSample == 16) {
            mBitsPerSample = AudioFormat.ENCODING_PCM_16BIT;
        }
        mRecorder = new AudioRecord(
                mAudioSource,
                mSampleRate,
                mChannels,
                mBitsPerSample,
                mBufferSize * 2
        );
        mBuffer = new short[mBufferSize];
    }

    public void start() {
        if (mRecorder == null) {
            return;
        }
        if (mRecorderThread != null) {
            return;
        }
        mRecorderThread = new AudioRecorderThread();
        mRecorderThread.start();
    }

    public void stop() {
        if (mRecorderThread == null) {
            return;
        }
        mRecorderThread.joinRecordThread();
        mRecorderThread = null;
    }

    public void close() {
        if (mRecorderThread != null) {
            return;
        }
        if (mRecorder == null) {
            return;
        }

        mRecorder.release();
        mRecorder = null;
    }

    private class AudioRecorderThread extends Thread {
        private volatile boolean mKeepAlive = true;

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            mRecorder.startRecording();

            while (mKeepAlive) {
                int bytesRead = mRecorder.read(mBuffer, 0, mBuffer.length);
                if (bytesRead > 0) {
                    mProcessor.process(mBuffer);
                } else {
                    if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                        mKeepAlive = false;
                    }
                }
            }

            mRecorder.stop();
        }

        public void joinRecordThread() {
            mKeepAlive = false;
            while (isAlive()) {
                try {
                    join();
                } catch (InterruptedException e) {}
            }
        }
    }

    public interface AudioProcessor {
        void process(short[] buffer);
    }
}
