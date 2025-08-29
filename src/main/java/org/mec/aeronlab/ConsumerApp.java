package org.mec.aeronlab;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.codecs.SourceLocation;
import jakarta.inject.Inject;
import org.mec.aeronlab.driver.EmbeddedMediaDriverProvider;
import org.mec.aeronlab.messaging.MarketDataSubscriber;
import org.mec.aeronlab.messaging.RecordingIdFetcher;
import org.mec.aeronlab.messaging.ReplayController;

public class ConsumerApp
{
    private final EmbeddedMediaDriverProvider driverProvider;

    @Inject
    public ConsumerApp(EmbeddedMediaDriverProvider driverProvider)
    {
        this.driverProvider = driverProvider;
    }

    public static void main(String[] args)
    {
        String liveChannel = "aeron:udp?endpoint=224.0.1.1:40123|interface=192.168.1.107";
        int liveStreamId = 1001;
        String replayChannel = "aeron:udp?endpoint=224.0.1.1:40124|interface=192.168.1.107";
        int replayStreamId = 2001;

        try (Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(driverProvider.aeronDirectory()));
             AeronArchive archive = AeronArchive.connect(new Aeron.Context().aeronDirectoryName(driverProvider.aeronDirectory())))
        {
            Publication publication = aeron.addPublication(liveChannel, liveStreamId);

            // Start the recording manually before fetchng the recording ID
            archive.startRecording(liveChannel, liveStreamId, SourceLocation.LOCAL);

            long recordingId = RecordingIdFetcher.fetchRecordingId(aeron, archive, publication);
            System.out.println("Recording ID: " + recordingId);

            ReplayController replayController = new ReplayController(
                    aeron, archive, replayChannel, replayStreamId, recordingId
            );

            MarketDataSubscriber subscriber = new MarketDataSubscriber(aeron, liveChannel, liveStreamId, replayController, 0);

            subscriber.run();
        }
    }
}
