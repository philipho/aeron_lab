package org.mec.aeronlab.archive;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.archive.Archive;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.client.RecordingDescriptorConsumer;
import io.aeron.archive.codecs.SourceLocation;
import org.mec.aeronlab.record.RecordingController;

import java.io.FileInputStream;
import java.util.Properties;

public class AeronArchiveLauncher
{
    private static final String DEFAULT_ARCHIVE_CONFIG_FILE = "archive.properties";
    private static final String STANDALONE_MEDIA_DRIVER_DIR = "./my-aeron-dir";

    public static void main(String[] args)
    {
        // Load properties from archive.properties
        loadArchiveProperties();

        String aeronDir = System.getenv("STANDALONE_MEDIA_DRIVER_DIR");
        if (aeronDir == null)
        {
            aeronDir = STANDALONE_MEDIA_DRIVER_DIR;
        }
        System.out.println("Starting Aeron Archive. aeronDir=" + aeronDir);

        Archive.Context ctx = new Archive.Context().aeronDirectoryName(aeronDir);
//        ctx.conclude(); // launch will call this internally.

        Archive archive = Archive.launch(ctx);

        System.out.println("Aeron Archive launched and running.");

//        defaultSubscriptions(archive);
    }

    private static void loadArchiveProperties()
    {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream((DEFAULT_ARCHIVE_CONFIG_FILE));)
        {
            props.load(in);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        for (String name : props.stringPropertyNames())
        {
            System.setProperty(name, props.getProperty(name));
            System.out.println("setting environ var name=" + name + ", value=" + props.getProperty(name));
        }
    }

    private static void defaultSubscriptions(Archive archive) {
        String[] channels = {
                "aeron:udp?endpoint=224.0.1.1:40123|interface=192.168.1.107"
        };

        int[] streamIds = {10};

        String requestChannel = "aeron:udp?endpoint=192.168.1.107:8010|interface=192.168.1.107";
        String responseChannel = "aeron:udp?endpoint=192.168.1.107:8020|interface=192.168.1.107";

        System.out.println("controlChannel [" + requestChannel + "]");
        System.out.println("responseChannel [" + responseChannel + "]");

        Aeron aeron = Aeron.connect(
                new Aeron.Context().aeronDirectoryName(STANDALONE_MEDIA_DRIVER_DIR));

        // Create a temporary publication to test connectivity
        Publication controlPublication = aeron.addPublication(
                requestChannel, // your control request channel
                100             // your control request stream ID
        );

        // Wait 60 seconds for connection
        long deadline = System.currentTimeMillis() + 60_000;
        while (!controlPublication.isConnected() && System.currentTimeMillis() < deadline)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }

        if (!controlPublication.isConnected())
        {
            throw new IllegalStateException("Control publication did not connect within timeout");
        }

        AeronArchive archiveClient = AeronArchive.connect(
                new AeronArchive.Context()
                        .aeron(aeron)
                        .controlRequestChannel(requestChannel)
                        .controlResponseChannel(responseChannel)
                        .controlRequestStreamId(100)
                        .controlResponseStreamId(101));

        for (int i = 0; i < channels.length; i++)
        {
            doRecroding(archiveClient, channels[i], streamIds[i]);
        }
    }

    private static void doRecroding(AeronArchive archiveClient, String channel, int streamId)
    {
        System.out.println(("Started recording: " + channel + " / streamId=" + streamId));

        archiveClient.startRecording(channel, streamId, SourceLocation.REMOTE);

        System.out.println("Started recording for: " + channel + "|" + streamId);

        // Wait briefly for recording to be registered
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException ignored)
        {
            // Ignored
        }
    }
}
