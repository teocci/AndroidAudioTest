package com.github.teocci.audiotest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by teocci on 8/9/16.
 */
public class MicRecorder extends Thread
{
    private static String TAG = "MicRecorder";

    public static MicRecorder instance = null;
    public static MicListener micListener = null;

    private int channel = AudioFormat.CHANNEL_IN_MONO;
    private int encoding = AudioFormat.ENCODING_PCM_16BIT;
    private int source = MediaRecorder.AudioSource.MIC;

    private int rate = 44100;                   // 44100 for music
    private int bufferSize = rate / 10 * 2;     // 0.1 seconds buffer size

    private int minBufSize = AudioRecord.getMinBufferSize(rate, channel, encoding);
    private boolean status = true;

    private boolean stopped = false;

    public MicRecorder(MicListener listener)
    {
        this.micListener = listener;
    }

    @Override
    public void run()
    {
        AudioRecord recorder = null;
        byte[] buffer;
        int size;

        Log.e(TAG, "Starting audio recording thread");

        stopped = false;

        try {
            // ... initialize
            buffer = initBuffer();

            if (recorder == null) {
                recorder = new AudioRecord(this.source,
                        this.rate, this.channel, this.encoding, this.bufferSize);
            }

            Log.e(TAG, "Recording started");
            recorder.startRecording();

            // ... loop
            while (!stopped) {
                size = recorder.read(buffer, 0,  bufferSize);
                micListener.onMicRecord(buffer, size);
            }

        } catch (Throwable x) {
            Log.w(TAG, "Error reading voice audio", x);
        } finally {
            if (recorder != null) recorder.stop();
            recorder = null;
        }
    }


    private byte[] initBuffer()
    {
        int minBufferSize = AudioRecord.getMinBufferSize(this.rate, this.channel, this.encoding);

        if (this.bufferSize <= minBufferSize)
            this.bufferSize = minBufferSize;

        Log.e(TAG, String.format("Audio bufferSize is %d bytes", bufferSize));

        return new byte[bufferSize];
    }

    public int getRate()
    {
        return rate;
    }

    public int getChannel()
    {
        return channel;
    }

    public int getEncoding()
    {
        return encoding;
    }

    public int getBuffersize()
    {
        return bufferSize;
    }

    public int getSource()
    {
        return source;
    }

    public void setRate(int rate)
    {
        this.rate = rate;
    }

    public void setChannel(int channel)
    {
        this.channel = channel;
    }

    public void setSource(int source)
    {
        this.source = source;
    }

    public void close()
    {
        stopped = true;
    }

    public void release() {
        instance = null;
        micListener = null;
        stopped = true;
    }

    public String toString()
    {
        return "Format:PCM_SIGNED " + rate + ".0 Hz, " + encoding + " bit, " + (channel == 1 ?
                "mono" : "stereo");
    }

    public static MicRecorder open(MicListener listener)
    {
        instance = new MicRecorder(listener);
        instance.start();
        return instance;
    }

    public static void setMicListener(MicListener listener)
    {
        micListener = listener;
    }
}
