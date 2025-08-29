package org.mec.aeronlab.messaging;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.status.RecordingPos;
import org.agrona.concurrent.status.CountersReader;

public class RecordingIdFetcher
{
    public static long fetchRecordingId(Aeron aeron, AeronArchive archive, Publication publication)
    {
        CountersReader counters = aeron.countersReader();
        int counterId = RecordingPos.findCounterIdBySession(counters, publication.sessionId());

        if (counterId == CountersReader.NULL_COUNTER_ID)
        {
            throw new IllegalStateException("Recording counter not found for sessionId: " + publication.sessionId());
        }

        return RecordingPos.getRecordingId(counters, counterId);
    }
}
