package org.mec.aeronlab.driver;

import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.agrona.concurrent.BackoffIdleStrategy;

public class StandaloneMediaDriver
{
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Launching Aeron Media Driver...");
            MediaDriver.launch(new MediaDriver.Context().aeronDirectoryName("./my-aeron-dir")
                    .threadingMode(ThreadingMode.SHARED)
                    .sharedIdleStrategy(new BackoffIdleStrategy()));
            System.out.println("Media Driver launched.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
