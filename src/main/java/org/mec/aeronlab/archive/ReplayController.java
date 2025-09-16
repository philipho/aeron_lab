package org.mec.aeronlab.archive;

import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.archive.client.AeronArchive;
import org.mec.aeronlab.util.MassQuoteProcessor;
import org.mec.aeronlab.MassQuoteProto;

import java.util.concurrent.atomic.AtomicLong;

public class ReplayController
{
    private final Aeron aeron;
    private final AeronArchive archive;
    private final String replayChannel;
    private final int replayStreamId;
    private final long recordingId;

    public ReplayController(Aeron aeron, AeronArchive archive, String replayChannel, int replayStreamId, long recordingId)
    {
        this.aeron = aeron;
        this.archive = archive;
        this.replayChannel = replayChannel;
        this.replayStreamId = replayStreamId;
        this.recordingId = recordingId;
    }

    public void recoverGap(long startSeq, long endSeq, SequencePositionTracker tracker)
    {
        Long startPos = tracker.lookupPosition(startSeq);
        Long endPos = tracker.lookupPosition(endSeq + 1);

        if (startPos == null || endPos == null)
        {
            System.out.println("Cannot recover gap: positions not found");
            return;
        }

        long replaySessionId = archive.startReplay(recordingId, startPos,
                endPos - startPos, replayChannel, replayStreamId);

        Subscription replaySubscription = aeron.addSubscription(replayChannel, replayStreamId);

        final AtomicLong expectedSeq = new AtomicLong(startSeq);

        while (expectedSeq.get() <= endSeq) {
            replaySubscription.poll((bufferData, offset, length, header) ->
            {
                byte[] data = new byte[length];
                bufferData.getBytes(offset, data);

                MassQuoteProto.MassQuote mq;
                try
                {
                     mq = MassQuoteProcessor.decodeMassQuote(data);
                }
                catch (Exception e)
                {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                long seq = Long.parseLong(mq.getQuoteId());

                if (seq == expectedSeq.get())
                {
                    System.out.println("Recovered MassQuote with OrderId=" + seq);
                    expectedSeq.incrementAndGet();
                }
                else
                {
                    System.out.println("Unexpected sequence in replay: seq=" + seq);
                }
            }, 10);
        }

        replaySubscription.close();
    }
}
