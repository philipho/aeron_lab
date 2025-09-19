package org.mec.chroniclelab.basic;

import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.threads.Pauser;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class HelloChronicleQueue
{
    final static String CHRONICLE_QUEUE_DIR = "queue-data";

    static Runnable writer = () ->
    {
        int idx = 1;
//        try (ChronicleQueue queue = ChronicleQueue.single(CHRONICLE_QUEUE_DIR))
        try (SingleChronicleQueue queue = SingleChronicleQueueBuilder.single(CHRONICLE_QUEUE_DIR)
                .rollCycle(CustomRollCycle.CUSTOM_ROLL_CYCLE)
                .build()
        )
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

    static class Reader implements Runnable
    {
        private volatile boolean isDone = false;
        private String name;
        private long startReadTime = 0;

        public Reader(String name)
        {
            this.name = name;
        }

        public void setStartReadTime(long startReadTime)
        {
            this.startReadTime = startReadTime;
        }

        @Override
        public void run()
        {
            Pauser pauser = net.openhft.chronicle.threads.Pauser.balanced();

//            try (ChronicleQueue queue = ChronicleQueue.single(CHRONICLE_QUEUE_DIR))
            try (SingleChronicleQueue queue =
                         SingleChronicleQueueBuilder.single(CHRONICLE_QUEUE_DIR)
                                 .rollCycle(CustomRollCycle.CUSTOM_ROLL_CYCLE)
                                 .build()
            )
            {
                ExcerptTailer tailer = queue.createTailer();

                // Read from specific time
                if (startReadTime > 0)
                {
                    CustomRollCycle rollCycle = (CustomRollCycle)queue.rollCycle();
                    int cycle = (int)(startReadTime / rollCycle.getCycleLengthMillis());
                    long fileSparseIndex = rollCycle.toIndex(cycle, 0);
                    tailer.moveToIndex(fileSparseIndex);
                }

                String msg;
                while (!isDone)
                {
                    msg = tailer.readText();

                    if (msg != null)
                    {
                        System.out.println(this.name + " - read: msg = " + msg);
                        pauser.reset();
                    }
                    else
                    {
                        pauser.pause();
                    }
                }
            }
        }

        public void isDone(boolean f)
        {
            this.isDone = f;
        }
    }

    //------------ Test ---------------------
    public static void testOneWriterTwoReaders() throws InterruptedException
    {
        Thread readerThread1 = new Thread(new Reader("Joe"));
        readerThread1.start();

        Thread.sleep(5_000);

        Thread writerThread = new Thread(writer);
        writerThread.start();

        Thread.sleep(5_000);

        Thread readerThread2 = new Thread(new Reader("Pete"));
        readerThread2.start();


        writerThread.join();
        readerThread1.join();
    }

    public static void testReadFromSpecificTime()
    {
        Reader reader = new Reader("read_from_sometime");
        LocalDateTime targetTime = LocalDateTime.of(2025, 9, 18, 15, 12, 2);
        long timestampMillis = 1_000L * (targetTime.atZone(ZoneId.systemDefault()).toEpochSecond());
        reader.setStartReadTime(timestampMillis);
        reader.run();
    }


    public static void main(String[] args) throws InterruptedException
    {
        testOneWriterTwoReaders();
//        testReadFromSpecificTime();
    }
}
