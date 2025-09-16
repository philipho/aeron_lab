package org.mec.aeronlab.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.archive.client.AeronArchive;
import jakarta.inject.Singleton;
import org.mec.aeronlab.record.RecordingController;

public class ArchiveModule extends AbstractModule
{
    // aeron:udp?endpoint=192.168.1.107:8010|interface=192.168.1.107
    private static final String AI = "aeron:udp?endpoint=192.168.1.107:8010|interface=0.0.0.0|mtu=1408|term-length=65536|sparse=true";
    private static final String STANDALONE_MEDIA_DRIVER_DIR = "./my-aeron-dir";
//    private static final String CONTROL_REQUEST_CHANNEL = "aeron:udp?endpoint=192.168.1.107:8010|interface=192.168.1.107";
    private static final String CONTROL_REQUEST_CHANNEL = "aeron:udp?endpoint=192.168.1.107:8010|interface=192.168.1.107";
    private static final int CONTROL_REQUEST_STREAMID = 100;
    private static final String CONTROL_RESPONSE_CHANNEL = "aeron:udp?endpoint=192.168.1.107:8020|interface=192.168.1.107";
    private static final int CONTROL_RESPONSE_STREAMID = 101;

    @Provides
    @Singleton
    public AeronArchive provideAeronArchive()
    {
        String aeronDir = System.getenv("STANDALONE_MEDIA_DRIVER_DIR");
        if (aeronDir == null)
        {
            aeronDir = STANDALONE_MEDIA_DRIVER_DIR;
        }
        System.out.println("ArchiveModule: aeronDir=" + aeronDir);

        Aeron.Context aeronContext = new Aeron.Context()
                .aeronDirectoryName(aeronDir);

        Aeron aeron = Aeron.connect(aeronContext);

        try
        {
            Thread.sleep(5_000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Create a temporary publication to verify connectivity
        Publication controlPublication = aeron.addPublication(CONTROL_REQUEST_CHANNEL, CONTROL_REQUEST_STREAMID);

        boolean connected = waitForPublicationConnected(controlPublication, 5000);
        if (!connected)
        {
            throw new IllegalStateException("Control publication did not connect within timeout");
        }

        // Connectivity is ok
        System.out.println("Connected to media driver. Try connecting to Aeron Archive...");
        AeronArchive.Context archiveContext =
                new AeronArchive.Context()
                        .aeronDirectoryName(aeronDir)
                        .controlRequestChannel(CONTROL_REQUEST_CHANNEL)
                        .controlRequestStreamId(CONTROL_REQUEST_STREAMID)
                        .controlResponseChannel(CONTROL_RESPONSE_CHANNEL)
                        .controlResponseStreamId(CONTROL_RESPONSE_STREAMID)
                        .controlMtuLength(1_408)
                        .controlTermBufferLength(65_536);

        try
        {
            Thread.sleep(100); // Wait for
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return AeronArchive.connect(archiveContext);
    }

    @Override
    public void configure()
    {
        bind(RecordingController.class).asEagerSingleton();
    }

    public static boolean waitForPublicationConnected(Publication publication, int timeoutMillis)
    {
        final long deadline = System.currentTimeMillis() + timeoutMillis;

        while (System.currentTimeMillis() < deadline)
        {
            if (publication.isConnected())
            {
                return true;
            }

            try
            {
                Thread.sleep(10); // Small delay to avoid busy-waiting
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false; // Timed out
    }
}
