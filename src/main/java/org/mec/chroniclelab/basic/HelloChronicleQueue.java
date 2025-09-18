package org.mec.chroniclelab.basic;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.threads.Pauser;

import java.io.File;
import java.time.LocalDateTime;

public class HelloChronicleQueue
{
    final static String CHRONICLE_QUEUE_DIR = "queue-data";

    static Runnable writer = () ->
    {
        int idx = 1;
        try (ChronicleQueue queue = ChronicleQueue.single(CHRONICLE_QUEUE_DIR))
        {
            ExcerptAppender appender = queue.createAppender();
            while (true)
            {
                try
                {
                    String msg = "writing: idx = " + idx++ + " time = " + LocalDateTime.now();
                    System.out.println("writing: " + msg);
                    appender.writeText(msg);
                    Thread.sleep(2_000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    };

    static Runnable reader = () ->
    {
        Pauser pauser = Pauser.balanced();

        try (ChronicleQueue queue = ChronicleQueue.single(CHRONICLE_QUEUE_DIR))
        {
            ExcerptTailer tailer = queue.createTailer();
            String msg;
            while (true)
            {
                msg = tailer.readText();

                if (msg != null)
                {
                    System.out.println("read: msg = " + msg);
                    pauser.reset();
                }
                else
                {
                    pauser.pause();
                }
            }
        }
    };

    public static void main(String[] args) throws InterruptedException
    {
        Thread writerThread = new Thread(writer);
        writerThread.start();

        Thread.sleep(5_000);

        Thread readerThread = new Thread(reader);
        readerThread.start();

        writerThread.join();
        readerThread.join();
    }
}
