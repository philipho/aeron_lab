package org.mec.aeronlab.messaging;

import jakarta.inject.Inject;
import org.mec.aeronlab.MassQuoteProcessor;

public class MessageLoop implements Runnable {
    private final AeronPublisher publisher;
    private final AeronSubscriber subscriber;

    @Inject
    public MessageLoop(AeronPublisher publisher, AeronSubscriber subscriber) {
        System.out.println("MessageLoop.ctor called...");
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("Message Loop " + i);
            byte[] msg = MassQuoteProcessor.encodeMassQuote();
            publisher.send(msg);
            subscriber.pollMassQuote();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
