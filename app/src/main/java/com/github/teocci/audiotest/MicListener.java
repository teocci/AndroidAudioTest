package com.github.teocci.audiotest;

/**
 * Created by teocci on 8/9/16.
 */
public interface MicListener
{
    public void onMicRecord(byte[] buffer, int bufferSize);
}
