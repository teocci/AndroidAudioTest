package com.github.teocci.audiotest;


import android.app.Activity;
import android.media.AudioRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity
{
    private static String TAG = "MainActivity";
    private Button startButton, stopButton;

    private MicRecorder micRecorder;
    private MicManager micManager;

    private boolean started = true;
    private SocketAudio threadAudio;

    private AudioRecord recorder = null;
    //private StreamingLoop audioLoop = null;

    private String remoteIP = "192.168.1.160";
    private int remotePortAudio = 9990;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        startButton.setOnClickListener(startListener);
        stopButton.setOnClickListener(stopListener);

        Toast.makeText(this, "New address: " + remoteIP + ":" + remotePortAudio, Toast.LENGTH_LONG).show();

        micManager = new MicManager(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeSocketClient();
        micManager.onPause();
        reset();
    }

    private void reset() {
        started = true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        micManager.onResume();
    }

    private final OnClickListener stopListener = new OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            started = false;
            recorder.release();
            Log.e(TAG, "Recorder released");
        }
    };

    private final OnClickListener startListener = new OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            started = true;
            startStreaming();
            Log.e(TAG, "Recorder Started");
        }
    };

    public void startStreaming()
    {
        threadAudio = new SocketAudio(micManager, remoteIP, remotePortAudio);
    }


    private void closeSocketClient() {
        if (threadAudio == null)
            return;

        threadAudio.interrupt();
        try {
            threadAudio.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        threadAudio = null;
    }
}