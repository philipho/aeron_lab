package org.mec.aeronlab.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import io.aeron.FragmentAssembler;
import org.mec.aeronlab.MassQuoteProcessor;
import org.mec.aeronlab.MassQuoteProto;
import org.mec.aeronlab.driver.EmbeddedMediaDriverProvider;
import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import jakarta.inject.Inject;

public class AeronSubscriber
{
    private final Subscription subscription;

    // tracker is injected
    private final SequencePositionTracker tracker;

    @Inject
    public AeronSubscriber(EmbeddedMediaDriverProvider driverProvider, SequencePositionTracker tracker)
    {
        System.out.println("AeronSubscriber.ctor called...");
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(driverProvider.aeronDirectory()));
//        subscription = aeron.addSubscription("aeron:ipc", 10);
        subscription = aeron.addSubscription("aeron:udp?endpoint=224.0.1.1:40123|interface=192.168.1.107", 10);
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
                System.out.println("AeronSubscriber.pollMassQuote: quoteId=" + quoteId + ", pos=" + pos);

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
    }
}
