package driver;

import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

public class EmbeddedMediaDriverProvider {
    private final MediaDriver mediaDriver;

    public EmbeddedMediaDriverProvider() {
        mediaDriver = MediaDriver.launch(
                new MediaDriver.Context()
                        .threadingMode(ThreadingMode.SHARED)
                        .dirDeleteOnStart(true)
        );
        System.out.println("Embedded Media Driver started.");
    }

    public String aeronDirectory() {
        return mediaDriver.aeronDirectoryName();
    }
}
