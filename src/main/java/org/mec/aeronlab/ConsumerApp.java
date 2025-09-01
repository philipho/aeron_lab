package org.mec.aeronlab;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.codecs.SourceLocation;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.mec.aeronlab.config.AeronModule;
import org.mec.aeronlab.driver.EmbeddedMediaDriverProvider;
import org.mec.aeronlab.messaging.MarketDataSubscriber;
import org.mec.aeronlab.messaging.MessageLoop;
import org.mec.aeronlab.messaging.RecordingIdFetcher;
import org.mec.aeronlab.messaging.ReplayController;

public class ConsumerApp
{
    public final EmbeddedMediaDriverProvider driverProvider;

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

        Injector injector = Guice.createInjector(new AeronModule());
        ConsumerApp app = injector.getInstance(ConsumerApp.class);

        try (Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(app.driverProvider.aeronDirectory()));
             AeronArchive archive = AeronArchive.connect(new AeronArchive.Context().aeronDirectoryName(app.driverProvider.aeronDirectory())))
        {
            Publication publication = aeron.addPublication(liveChannel, liveStreamId);

            // Start the recording manually before fetchng the recording ID
            archive.startRecording(liveChannel, liveStreamId, SourceLocation.LOCAL);

            long recordingId = RecordingIdFetcher.fetchRecordingId(aeron, archive, publication);
            System.out.println("Recording ID: " + recordingId);

            ReplayController replayController = new ReplayController(
                    aeron, archive, replayChannel, replayStreamId, recordingId
            );

            MarketDataSubscriber subscriber = new MarketDataSubscriber(
                    aeron, liveChannel, liveStreamId, replayController, 0);

            subscriber.run();
        }
    }
}
