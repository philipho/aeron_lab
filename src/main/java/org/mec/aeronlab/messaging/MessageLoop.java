package org.mec.aeronlab.messaging;

import jakarta.inject.Inject;

public class MessageLoop implements Runnable {
    private final AeronPublisher publisher;
    private final AeronSubscriber subscriber;

    @Inject
    public MessageLoop(AeronPublisher publisher, AeronSubscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            String msg = "Hello Aeron #" + i;
            publisher.send(msg);
            subscriber.poll();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
