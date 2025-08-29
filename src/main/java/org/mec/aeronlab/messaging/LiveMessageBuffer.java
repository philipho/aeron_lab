package org.mec.aeronlab.messaging;

import org.mec.aeronlab.MassQuoteProto;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LiveMessageBuffer
{
    private final BlockingQueue<MassQuoteProto.MassQuote> buffer = new LinkedBlockingQueue<>();
    private volatile boolean buffering = false;

    public void startBuffering()
    {
        buffering = true;
    }

    public void stopBuffering()
    {
        buffering = false;
    }

    public void handleLiveMessage(MassQuoteProto.MassQuote mq)
    {
        if (buffering)
        {
            buffer.offer(mq);
        }
        else
        {
            System.out.println("LiveMessageBuffer.handlLiveMessage: MassQuote=" + mq);
        }
    }

    public void flushBuffer(long expectedNextSeq)
    {
        while (!buffer.isEmpty())
        {
            MassQuoteProto.MassQuote mq = buffer.peek();
            if (Long.parseLong(mq.getQuoteId()) == expectedNextSeq)
            {
                System.out.println("LiveMessageBuffer.flushBuffer: MassQuote=" + mq);
                expectedNextSeq++;
            }
            else
            {
                break; // wait for correct sequence
            }
        }
    }
}
