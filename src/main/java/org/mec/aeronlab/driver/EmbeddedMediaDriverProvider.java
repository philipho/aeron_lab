package org.mec.aeronlab.driver;

import io.aeron.archive.Archive;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

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
                .aeronDirectoryName(mediaDriverCtx.aeronDirectoryName());

        archive = Archive.launch(archiveCtx);

        System.out.println("Embedded Media Driver started.");
    }

    public String aeronDirectory()
    {
        return mediaDriver.aeronDirectoryName();
    }
}
