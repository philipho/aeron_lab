package org.mec.aeronlab.archive;

import io.aeron.Aeron;
import io.aeron.archive.Archive;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.codecs.SourceLocation;

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

        Archive.launch(ctx);

        System.out.println("Aeron Archive launched and running.");

        // Preset recording
        presetRecording(ctx);
    }

    private static void presetRecording(Archive.Context ctx)
    {
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(STANDALONE_MEDIA_DRIVER_DIR));
        AeronArchive archive = AeronArchive.connect(new AeronArchive.Context()
                .aeron(aeron)
                .aeronDirectoryName(STANDALONE_MEDIA_DRIVER_DIR));

        String[] channels = {
                "aeron:udp?endpoint=224.0.1.1:40123|interface=192.168.1.107"
        };

        int[] streamIds = {10};

        for (int i = 0; i < channels.length; i++)
        {
            archive.startRecording(channels[i], streamIds[i], SourceLocation.LOCAL);
            System.out.println(("Started recording: " + channels[i] + " / streamId=" + streamIds[i]));
        }

        System.out.println("All recrodings started");
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
}
