package org.mec.aeronlab;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.mec.aeronlab.config.AeronModule;
import org.mec.aeronlab.messaging.AeronSubscriber;
import org.mec.aeronlab.messaging.MessageLoop;

/*
Tinker with:

Thread.sleep() for pacing

Buffer sizes

Idle strategies

Switching to UDP (aeron:udp?endpoint=localhost:40123)
 */
public class Main {
    public static void main(String[] args) throws InterruptedException
    {
        Injector injector = Guice.createInjector(new AeronModule());
        MessageLoop loop = injector.getInstance(MessageLoop.class);
//        Thread t = new Thread(loop);
//        t.start();
        Thread.sleep(1_000);
        loop.run();
    }
}
