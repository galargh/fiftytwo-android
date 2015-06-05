package com.gfjalar.fiftytwo;


import android.app.Activity;

import android.os.Bundle;

import android.widget.FrameLayout;
import android.widget.TextView;


public class MainActivity extends Activity {
    private FrameLayout mBackgroundLayout;
    private TextView mFrequencyView;
    private TextView mDifferenceView;
    private TextView mNoteView;

    AudioRecorder recorder;

    private void startRecorder() {
        if(recorder == null) {
            recorder = new AudioRecorder(1, 16, 4096, new AudioRecorder.AudioProcessor() {
                MPM mpm = new MPM(44100, 0.75, 0.95, 80);

                @Override
                public void process(short[] buffer) {
                    double pitch = mpm.detectPitch(buffer);
                    Settings settings = new Settings(pitch);
                    updateUI(settings);
                }
            });
        }
        recorder.start();
    }

    private void stopRecorder() {
        if(recorder != null) {
            recorder.stop();
            recorder.close();
            recorder = null;
        }
    }

    private void updateUI(final Settings settings) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBackgroundLayout.setBackgroundColor(settings.mColor);
                mFrequencyView.setText(settings.mFrequencyString);
                mDifferenceView.setText(settings.mDifferenceString);
                mNoteView.setText(settings.mNoteString);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mBackgroundLayout = (FrameLayout)findViewById(R.id.frame);
        mFrequencyView = (TextView)findViewById(R.id.frequency);
        mDifferenceView = (TextView)findViewById(R.id.difference);
        mNoteView = (TextView)findViewById(R.id.note);

        startRecorder();

    }

    @Override
    protected void onResume() {
        super.onResume();

        startRecorder();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopRecorder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopRecorder();
    }
}
