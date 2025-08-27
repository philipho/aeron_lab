package org.mec.aeronlab.messaging;

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
        Aeron aeron = Aeron.connect(new Aeron.Context()
                .aeronDirectoryName(driverProvider.aeronDirectory()));
        publication = aeron.addPublication("aeron:ipc", 10);
    }

    public void send(String message) {
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(256));
        buffer.putStringWithoutLengthAscii(0, message);
        long result = publication.offer(buffer, 0, buffer.capacity());
        System.out.println("Sent: " + message + " (result=" + result + ")");
    }
}
