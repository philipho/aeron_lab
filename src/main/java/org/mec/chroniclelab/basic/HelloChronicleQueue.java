package org.mec.chroniclelab.basic;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;

import java.io.File;

public class HelloChronicleQueue
{
    public static void main(String[] args)
    {
        String queueDir = "queue-data";

        // Create the queue
        try (ChronicleQueue queue = ChronicleQueue.single(queueDir))
        {
            // Write to queue
            ExcerptAppender appender = queue.createAppender();
            appender.writeText("Hello Chronicle!");
            appender.writeText("Market data: AAPL 189.23");

            // Read from queue
            ExcerptTailer tailer = queue.createTailer();
            String msg;
            while ((msg = tailer.readText()) != null)
            {
                System.out.println("Read: " + msg);
            }
        }
    }
}
