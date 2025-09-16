package org.mec.aeronlab.record;

public interface Controller
{
    void startRecording(String channel, int streamId);
    void stopRecording(String channel, int streamId);
}
