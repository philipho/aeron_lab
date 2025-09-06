package org.mec.aeronlab.messaging;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.protobuf.InvalidProtocolBufferException;
import io.aeron.FragmentAssembler;
import io.aeron.Image;
import org.mec.aeronlab.MassQuoteProcessor;
import org.mec.aeronlab.MassQuoteProto;
import org.mec.aeronlab.config.AeronModule;
import org.mec.aeronlab.driver.EmbeddedMediaDriverProvider;
import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import jakarta.inject.Inject;

public class AeronSubscriber
{
    private static final String STANDALONE_MEDIA_DRIVER_DIR = "./scripts/my-aeron-dir";
    private final Subscription subscription;
    // tracker is injected
    private final SequencePositionTracker tracker;

    @Inject
//    public AeronSubscriber(EmbeddedMediaDriverProvider driverProvider, SequencePositionTracker tracker)
    public AeronSubscriber(SequencePositionTracker tracker)
    {
        System.out.println("AeronSubscriber.ctor called...");
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(STANDALONE_MEDIA_DRIVER_DIR));
//        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(driverProvider.aeronDirectory()));
//        subscription = aeron.addSubscription("aeron:ipc", 10);
        String aeronInterface = System.getenv("AERON_INTERFACE");
        System.out.println("Subscription AERON_INTERFACE aeronInterface=[" + aeronInterface + "]");

        if (aeronInterface == null)
        {
            subscription = aeron.addSubscription("aeron:udp?endpoint=224.0.1.1:40123|interface=192.168.1.107", 10);
        }
        else
        {
            subscription = aeron.addSubscription("aeron:udp?endpoint=224.0.1.1:40123|interface=" + aeronInterface, 10);
        }

        this.tracker = tracker;
    }

    public void poll()
    {
        FragmentHandler handler =
                (buffer, offset, length, header) -> {
                    String received = buffer.getStringWithoutLengthAscii(offset, length);
                    System.out.println("Received: " + received);
                };
        System.out.println("AeronSubscriber.poll ready to poll...");
        subscription.poll(handler, 10);
    }

    public void pollMassQuote()
    {
        FragmentHandler handler = (buffer, offset, length, header) ->
        {
            byte[] data = new byte[length];
            buffer.getBytes(offset, data);

            try
            {
                MassQuoteProto.MassQuote mq = MassQuoteProcessor.decodeMassQuote(data);
                long quoteId = Long.parseLong(mq.getQuoteId());
                long pos = header.position();
                int sessionId = header.sessionId();
                int streamId = header.streamId();
                int initialTermId = header.initialTermId();
                int termOffset = header.termOffset();
                int frameLength = header.frameLength();
                System.out.println("AeronSubscriber.pollMassQuote: quoteId=" + quoteId
                        + ", pos=" + pos);

                String log = String.format("AeronSubscriber.pollMassQuote: quoteId=%s, pos=%d, sessionId=%d, " +
                                "streamId=%d, initialTermId=%d, termOffset=%d, frameLength=%d",
                                quoteId, pos, sessionId, streamId, initialTermId, termOffset, frameLength);
                System.out.println(log);

                tracker.record(quoteId, pos);
            }
            catch (InvalidProtocolBufferException e)
            {
                throw new RuntimeException(e);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        };

        FragmentHandler assembler = new FragmentAssembler(handler);

        subscription.poll(assembler, 10);

        // Diagnostics
//        if (!subscription.isConnected())
//        {
//            System.out.println("Subscription is connected");
//        }
//        else {
//            System.out.println("Subscritpion is NOT connected");
//        }
//
//        int imageCount = subscription.imageCount();
//        System.out.println("📷 Image count: " + imageCount);
//
//        for (int i = 0; i < imageCount; i++) {
//            Image image = subscription.imageAtIndex(i);
//            long position = image.position();
//            long joinPosition = image.joinPosition();
//
//            System.out.printf("📊 Image[%d] Position: %d | Join: %d%n", i, position, joinPosition);
//
//            if (image.isClosed()) {
//                System.out.println("🚫 Image is closed");
//            }
//        }
    }

    public static void main(String[] args)
    {
        Injector injector = Guice.createInjector(new AeronModule());
        AeronSubscriber subscriber = injector.getInstance(AeronSubscriber.class);
        boolean isDone = false;
        int msgIdx = 1;

        while (!isDone)
        {
            subscriber.pollMassQuote();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
