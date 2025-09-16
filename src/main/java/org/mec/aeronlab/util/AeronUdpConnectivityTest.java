package org.mec.aeronlab.util;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class AeronUdpConnectivityTest
{
    // aeron:udp?endpoint=224.0.1.1:40123|interface=192.168.1.107
    private static final String CHANNEL = "aeron:udp?endpoint=192.168.1.107:8010|interface=192.168.1.107";
//    private static final String CHANNEL = "aeron:ipc";
    private static final int STREAM_ID = 100;

    public static void main(String[] args) throws Exception
    {
        // Start embedded MediaDriver for testing
        MediaDriver.Context driverCtx = new MediaDriver.Context()
                .dirDeleteOnStart(true); // Clean start

        try (MediaDriver driver = MediaDriver.launch(driverCtx);
             Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(driver.aeronDirectoryName()));)
        {
            // Create publication and subscription
            Publication publication = aeron.addPublication(CHANNEL, STREAM_ID);

            Subscription subscription = aeron.addSubscription(CHANNEL, STREAM_ID);

            try {
                Thread.sleep(1_000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // Prepare message
            UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(128));
            String message = "Hello Aeron!";
            buffer.putBytes(0, message.getBytes());

            // Send message
            long result = publication.offer(buffer, 0, message.length());
            System.out.println("Publication result: " + result);

            // Poll for message
            System.out.println("Waiting for message...");
            for (int i = 0; i < 100; i++)
            {
                int fragments = subscription.poll((buffer1, offset, length, header) -> {
                    byte[] data = new byte[length];
                    buffer1.getBytes(offset, data);
                    System.out.println("Received: " + new String(data));
                }, 10);

                if (fragments > 0)
                {
                    break;
                }

                Thread.sleep(10);
            }
        }
    }
}

