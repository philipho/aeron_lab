package org.mec.aeronlab.record;

import io.aeron.Aeron;
import io.aeron.archive.Archive;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.client.RecordingDescriptorConsumer;
import io.aeron.archive.codecs.SourceLocation;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RecordingController implements Controller
{
    AeronArchive archive;

    // Store the record key and metadata
    private final Map<String, RecordingMetadata> recordingRegistry = new ConcurrentHashMap<>();

    // From AeronArchiveModule
    @Inject
    public RecordingController(AeronArchive archive)
    {
        this.archive = archive;
    }

    @Override
    public void startRecording(String channel, int streamId)
    {
        String key = channel + "|" + streamId;

        archive.startRecording(channel, streamId, SourceLocation.REMOTE);
        System.out.println("Started recording for: " + key);

        // Wait briefly for recording to be registered
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException ignored)
        {
            // Ignored
        }

        final RecordingDescriptorConsumer consumer =
                (controlSessionId, correlationId, recordingId,
                 startTimestamp, stopTimestamp, startPosition,
                 stopPosition, initialTermId, segmentFileLength,
                 termBufferLength, mtuLength, sessionId,
                 aStreamId, strippedChannel, originalChannel,
                 sourceIdentity) ->
                {
                    RecordingMetadata metadata = new RecordingMetadata(
                            recordingId,
                            sessionId,
                            startPosition,
                            stopPosition,
                            System.currentTimeMillis()
                    );
                    recordingRegistry.put(key, metadata);
                    System.out.println("Tracking recording: " + metadata);
                };

        int numRecordsFound = archive.listRecordings(0, 100, consumer);
        System.out.println("Number of record descriptors found = " + numRecordsFound);
    }

    @Override
    public void stopRecording(String channel, int streamId)
    {
        String key = channel + "|" + streamId;
        RecordingMetadata metadata = recordingRegistry.get(key);

        if (metadata != null)
        {
            archive.stopRecording(metadata.recordingId);
            System.out.println("Stopped recording for: " + key);
        }
        else
        {
            System.out.println("No active recording found for: " + key);
        }
    }

    // DTO
    record RecordingMetadata(
            long recordingId,
            int sessionId,
            long startPosition,
            long stopPosition,
            long startAt // Recording start time
    ){}
}
