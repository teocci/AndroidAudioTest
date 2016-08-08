package com.github.teocci.audiotest;


import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends Activity
{
    private Button startButton, stopButton;

    private AudioRecord recorder = null;
    //private StreamingLoop audioLoop = null;


    private int port = 9990;
    private int sampleRate = 44100; // 44100 for music
    private int channel = AudioFormat.CHANNEL_IN_MONO;
    private int encoding= AudioFormat.ENCODING_PCM_16BIT;

    private int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding);
    private boolean status = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        startButton.setOnClickListener(startListener);
        stopButton.setOnClickListener(stopListener);
    }

    private final OnClickListener stopListener = new OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            status = false;
            recorder.release();
            Log.d("VS", "Recorder released");
        }
    };

    private final OnClickListener startListener = new OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            status = true;
            startStreaming();
        }
    };

    public void startStreaming()
    {
        Thread streamThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    Log.e("VS", "Socket Created");

                    byte[] buffer = new byte[minBufSize];

                    Log.e("VS", "Buffer created of size " + minBufSize);
                    DatagramPacket packet;

                    final InetAddress destination = InetAddress.getByName("192.168.1.160");
                    Log.e("VS", "Address retrieved");

                    initAudio();
                    Log.e("VS", "Recorder initialized");

                    recorder.startRecording();

                    while (status == true) {
                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);

                        //putting buffer in the packet
                        packet = new DatagramPacket(buffer, buffer.length, destination, port);

                        socket.send(packet);
                        System.out.println("MinBufferSize: " + minBufSize);
                    }
                } catch (UnknownHostException e) {
                    Log.e("VS", "UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("VS", "IOException");
                }
            }

        });
        streamThread.start();
    }

    private void initAudio() {
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding);
        int minTargetSize = sampleRate / 10 * 2;      // 0.1 seconds buffer size
        if (minTargetSize < minBufferSize) {
            minTargetSize = minBufferSize;
        }
        if (recorder == null) {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channel,
                    encoding,
                    minTargetSize);
        }
    }
}