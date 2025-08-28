package org.mec.aeronlab.messaging;

import org.mec.aeronlab.MassQuoteProto;
import org.mec.aeronlab.driver.EmbeddedMediaDriverProvider;
import io.aeron.Aeron;
import io.aeron.Publication;
import jakarta.inject.Inject;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class AeronPublisher {
    private final Publication publication;

    @Inject
    public AeronPublisher(EmbeddedMediaDriverProvider driverProvider) {
        System.out.println("AeroPublisher.ctor called...");
        Aeron aeron = Aeron.connect(new Aeron.Context()
                .aeronDirectoryName(driverProvider.aeronDirectory()));
//        publication = aeron.addPublication("aeron:ipc", 10);
        // Have to specify the interface parameter otherwise it won't work on local PC
        publication = aeron.addPublication("aeron:udp?endpoint=224.0.1.1:40123|interface=192.168.1.107", 10);
    }

    public void send(byte[] payload) {
        UnsafeBuffer buffer = new UnsafeBuffer(payload);
        long result = publication.offer(buffer);

        if (result < 0) {
            System.out.println("Failed to send: " + result);
        } else {
            System.out.println("Sent " + payload.length + " bytes");
        }
    }

    public void send(String message) {
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(256));
        buffer.putStringWithoutLengthAscii(0, message);
        long result = publication.offer(buffer, 0, buffer.capacity());
        System.out.println("Sent: " + message + " (result=" + result + ")");
    }
}
