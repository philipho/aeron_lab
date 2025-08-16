package messaging;

import driver.EmbeddedMediaDriverProvider;
import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import jakarta.inject.Inject;

public class AeronSubscriber {
    private final Subscription subscription;

    @Inject
    public AeronSubscriber(EmbeddedMediaDriverProvider driverProvider) {
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(driverProvider.aeronDirectory()));
        subscription = aeron.addSubscription("aeron:ipc", 10);
    }

    public void poll() {
        FragmentHandler handler =
                (buffer, offset, length, header) -> {
                    String received = buffer.getStringWithoutLengthAscii(offset, length);
                    System.out.println("Received: " + received);
                };
        System.out.println("AeronSubscriber.poll ready to poll...");
        subscription.poll(handler, 10);
    }
}
