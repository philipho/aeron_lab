import com.google.inject.Guice;
import com.google.inject.Injector;
import config.AeronModule;
import messaging.MessageLoop;

/*
Tinker with:

Thread.sleep() for pacing

Buffer sizes

Idle strategies

Switching to UDP (aeron:udp?endpoint=localhost:40123)
 */
public class Main {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AeronModule());
        MessageLoop loop = injector.getInstance(MessageLoop.class);
//        Thread t = new Thread(loop);
//        t.start();
        loop.run();
    }
}
