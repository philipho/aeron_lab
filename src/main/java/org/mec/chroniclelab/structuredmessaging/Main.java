package org.mec.chroniclelab.structuredmessaging;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.threads.Pauser;
import net.openhft.chronicle.wire.DocumentContext;

public class Main
{
    private static final String QUEUE_DIR = "market-data";

    public static void writeMarketData()
    {
        final int RECORDS_TO_WRITE = 1_000;
        final MarketData marketData = new MarketData();
        final ChronicleQueue q = ChronicleQueue.single(QUEUE_DIR);
        final ExcerptAppender appender = q.createAppender();

        long start = System.nanoTime();
        for (long i = 0; i < RECORDS_TO_WRITE; i++)
        {
            try (final DocumentContext doc = appender.acquireWritingDocument(false))
            {
                doc.wire()
                        .bytes()
                        .writeObject(MarketData.class, MarketData.recycle(marketData));
            }
        }
        long timeTakenMillis = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Time taken to write 1 billion MarketData to Chronicle Queue = " + timeTakenMillis + " ms.");
    }

    public static void readMarketData()
    {
        final ChronicleQueue q = ChronicleQueue.single(QUEUE_DIR);
        final ExcerptTailer tailer = q.createTailer();
        final Pauser pauser = Pauser.balanced();

        while (true)
        {
            try (final DocumentContext doc = tailer.readingDocument())
            {
                if (doc.isPresent())
                {
                    MarketData marketData = doc.wire()
                            .bytes()
                            .readObject(MarketData.class);

                    System.out.println(marketData);
                    pauser.reset();
                }
                else
                {
                    pauser.pause();
                }
            }
        }
    }

    static class Writer implements Runnable
    {
        @Override
        public void run()
        {
            writeMarketData();
        }
    }

    static class Reader implements Runnable
    {
        @Override
        public void run()
        {
            readMarketData();
        }
    }

    //------- Test -----------
    public static void main(String[] args) throws InterruptedException
    {
        Thread readThread = new Thread(new Reader());
        Thread writeThread = new Thread(new Writer());

        readThread.start();
        Thread.sleep(5_000);
        writeThread.start();
    }
}
