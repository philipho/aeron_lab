package org.mec.aeronlab.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import io.aeron.Aeron;
import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.mec.aeronlab.archive.LiveMessageBuffer;
import org.mec.aeronlab.archive.ReplayController;
import org.mec.aeronlab.archive.SequencePositionTracker;
import org.mec.aeronlab.util.MassQuoteProcessor;
import org.mec.aeronlab.MassQuoteProto;

public class MarketDataSubscriber implements AutoCloseable
{
    private final Aeron aeron;
    private final Subscription liveSubscription;
    private final SequencePositionTracker tracker;
    private final LiveMessageBuffer buffer;
    private final ReplayController replayController;
    private volatile boolean isDone = false;

    private long expectedSequence;

    public MarketDataSubscriber(
            Aeron aeron,
            String liveChannel,
            int liveStreamId,
            ReplayController replayController,
            long startingSequence)
    {
        this.aeron = aeron;
        this.liveSubscription = aeron.addSubscription(liveChannel, liveStreamId);
        this.tracker = new SequencePositionTracker();
        this.buffer = new LiveMessageBuffer();
        this.replayController = replayController;
        this.expectedSequence = startingSequence;
    }

    public void run()
    {
        while (!isDone)
        {
            liveSubscription.poll((bufferData, offset, length, header) ->
            {
                byte[] data = new byte[length];
                bufferData.getBytes(offset, data);

                try
                {
                    MassQuoteProto.MassQuote mq = MassQuoteProcessor.decodeMassQuote(data);
                    long seq = Long.parseLong(mq.getQuoteId());
                    long pos = header.position();
                    System.out.println("MarketDataSubscriber.pollMassQuote: quoteId=" + seq + ", pos=" + pos);

                    tracker.record(seq, pos);

                    if (seq == expectedSequence) {
                        System.out.println("Processed quoteId=" + seq);
                        expectedSequence++;
                    } else if (seq > expectedSequence) {
                        System.out.printf("Gap detected: expected %d but got %d%n", expectedSequence, seq);

                        buffer.startBuffering();
                        replayController.recoverGap(expectedSequence, seq - 1, tracker);
                        expectedSequence = seq;
                        buffer.stopBuffering();
                        buffer.flushBuffer(expectedSequence);
                    }
                    else
                    {
                        System.out.printf("Duplicate or late message: %d%n", seq);
                    }
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

            }, 10);

            try
            {
                Thread.sleep(5); // avoid busy spin
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void pollMassQuote(int fragmentLimit)
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
                System.out.println("MarketDataSubscriber.pollMassQuote: quoteId=" + quoteId + ", pos=" + pos);

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

        liveSubscription.poll(assembler, fragmentLimit);
    }

    public void close()
    {
        liveSubscription.close();
        aeron.close();
    }
}
