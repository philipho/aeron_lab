package org.mec.aeronlab.messaging;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.mec.aeronlab.MassQuoteProcessor;
import org.mec.aeronlab.MassQuoteProto;
import org.mec.aeronlab.config.AeronModule;
import org.mec.aeronlab.driver.EmbeddedMediaDriverProvider;
import io.aeron.Aeron;
import io.aeron.Publication;
import jakarta.inject.Inject;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class AeronPublisher {
    private static final String STANDALONE_MEDIA_DRIVER_DIR = "./scripts/my-aeron-dir";
    private final Publication publication;

    @Inject
//    public AeronPublisher(EmbeddedMediaDriverProvider driverProvider) {
    public AeronPublisher()
    {
        System.out.println("AeroPublisher.ctor called...");
        Aeron aeron = Aeron.connect(new Aeron.Context()
                .aeronDirectoryName(STANDALONE_MEDIA_DRIVER_DIR));
//                .aeronDirectoryName(driverProvider.aeronDirectory()));
//        publication = aeron.addPublication("aeron:ipc", 10);
        // Have to specify the interface parameter otherwise it won't work on local PC
        String aeronInterface = System.getenv("AERON_INTERFACE");
        System.out.println("Publication AERON_INTERFACE aeronInterface=[" + aeronInterface + "]");

        if (aeronInterface == null)
        {
            publication = aeron.addPublication("aeron:udp?endpoint=224.0.1.1:40123|interface=192.168.1.107", 10);
        }
        else
        {
            publication = aeron.addPublication("aeron:udp?endpoint=224.0.1.1:40123|interface=" + aeronInterface, 10);
        }
    }

    public void send(byte[] payload)
    {
        UnsafeBuffer buffer = new UnsafeBuffer(payload);
        long result;
        do {
            result = publication.offer(buffer);
            if (result < 0) {
                System.out.println("Publisher offer failed, result = " + result);
                try
                {
                    Thread.sleep(5); // back off briefly
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                System.out.println("Publisher offer succeeded, result = " + result);
            }
        } while (result < 0);
        System.out.println("Sent " + payload.length + " bytes");
    }

    public void send(String message) {
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(256));
        buffer.putStringWithoutLengthAscii(0, message);
        long result = publication.offer(buffer, 0, buffer.capacity());
        System.out.println("Sent: " + message + " (result=" + result + ")");
    }

    public static void main(String[] args)
    {
        Injector injector = Guice.createInjector(new AeronModule());
        AeronPublisher publisher = injector.getInstance(AeronPublisher.class);
        int numOfMsgToSend = 100;

        for (int i = 0; i < numOfMsgToSend; i++)
        {
            byte[] msg = MassQuoteProcessor.encodeMassQuote(i);
            System.out.println("Publisher sending = " + i + ", bytes = " + msg.length);
            publisher.send(msg);
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Publish sent " + numOfMsgToSend + " messages.");
    }
}
