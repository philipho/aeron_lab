package org.mec.aeronlab.driver;

import io.aeron.archive.Archive;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

import static io.aeron.archive.Archive.Configuration.replicationChannel;

public class EmbeddedMediaDriverProvider {
    private final MediaDriver mediaDriver;
    private final Archive archive;

    public EmbeddedMediaDriverProvider()
    {
        MediaDriver.Context mediaDriverCtx = new MediaDriver.Context()
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnStart(true);

        mediaDriver = MediaDriver.launchEmbedded(mediaDriverCtx);

        Archive.Context archiveCtx = new Archive.Context()
                .aeronDirectoryName(mediaDriverCtx.aeronDirectoryName())
                .controlChannel("aeron:udp?endpoint=localhost:8010") // Set the control channel
                .localControlChannel("aeron:ipc") // Also set the local control channel for IPC access
                .replicationChannel("aeron:udp?endpoint=localhost:8020"); // Add the replication channel here
//                .controlRequestChannel("aeron:udp?endpoint=localhost:8010") // Must match Archive's controlChannel
//                .controlResponseChannel("aeron:udp?endpoint=localhost:8011"); // A dedicated channel for responses


        archive = Archive.launch(archiveCtx);

        System.out.println("Embedded Media Driver started.");
    }

    public String aeronDirectory()
    {
        return mediaDriver.aeronDirectoryName();
    }
}
