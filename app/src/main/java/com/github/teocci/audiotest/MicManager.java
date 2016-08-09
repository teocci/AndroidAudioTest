package com.github.teocci.audiotest;

import android.content.Context;
import android.widget.Toast;

import java.util.LinkedList;

/**
 * Created by teocci on 8/9/16.
 */
public class MicManager
{
    private Context context;
    private static MicRecorder micRecorder;

    private static LinkedList<byte[]> queue = new LinkedList<>();
    private static final int MAX_BUFFER = 15;
    private byte[] lastChunk = null;
    private int chunkLength;

    public MicManager(Context contxt)
    {
        context = contxt;
        // Create an instance of Camera
        micRecorder = getMicInstance();
    }

    public MicRecorder getMicRecorder()
    {
        return micRecorder;
    }

    private void releaseMic()
    {
        if (micRecorder != null) {
            // release the camera for other applications
            micRecorder.release();
            micRecorder = null;
        }
    }

    public void onPause()
    {
        releaseMic();
        resetBuff();
    }

    public void onResume()
    {
        if (micRecorder == null) {
            micRecorder = getMicInstance();
        }

        Toast.makeText(context,
                "Buffer size: " + micRecorder.getBuffersize() + " " + micRecorder.toString(),
                Toast.LENGTH_LONG).show();
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private static MicRecorder getMicInstance()
    {
        MicRecorder mr = null;
        try {
            mr = micRecorder.open(new MicListener() {
                @Override
                public void onMicRecord(byte[] buffer, int bufferSize)
                {
                    // TODO Auto-generated method stub
                    synchronized (queue) {
                        if (queue.size() == MAX_BUFFER) {
                            queue.poll();
                        }
                        queue.add(buffer);
                    }
                }

            }); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return mr; // returns null if camera is unavailable
    }

    public byte[] getMicBuffer()
    {
        synchronized (queue) {
            if (queue.size() > 0) {
                lastChunk = queue.poll();
            }
        }

        return lastChunk;
    }

    private void resetBuff()
    {
        synchronized (queue) {
            queue.clear();
            lastChunk = null;
        }
    }
}
