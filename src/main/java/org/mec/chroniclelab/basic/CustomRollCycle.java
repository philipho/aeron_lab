package org.mec.chroniclelab.basic;

import net.openhft.chronicle.queue.RollCycle;

public class CustomRollCycle implements RollCycle
{
    // Roll cycle length - 2 minutes. So generate one file every two minutes
    private static final int CYCLE_LENGTH_MILLIS = 2 * 60 * 1000;

    // File format
    private static final String FORMAT = "yyyyMMdd-HHmm";

    // Sequence bits.
    // The number of bits in the index that are reserved for the sequence number.
    // This value is determined by the maximum number of messages you expect per cycle.
    private static final int SEQUENCE_BITS = 32;

    // Cycle name
    public static final RollCycle CUSTOM_ROLL_CYCLE = new CustomRollCycle();


    public long getCycleLengthMillis()
    {
        return CYCLE_LENGTH_MILLIS;
    }

    @Override
    public String format()
    {
        return FORMAT;
    }

    @Override
    public int lengthInMillis()
    {
        return CYCLE_LENGTH_MILLIS;
    }

    @Override
    public int defaultIndexCount()
    {
        return 4096;
    }

    @Override
    public int defaultIndexSpacing()
    {
        return 16;
    }

    @Override
    public long toIndex(int cycle, long sequenceNumber)
    {
        return ((long) cycle << SEQUENCE_BITS) + sequenceNumber;
    }

    @Override
    public long toSequenceNumber(long index)
    {
        long mask = (1L << SEQUENCE_BITS) - 1;
        return (index & mask);
    }

    @Override
    public int toCycle(long index)
    {
        return (int) (index >> SEQUENCE_BITS);
    }

    @Override
    public long maxMessagesPerCycle()
    {
        // Default - no explicit message count limit.
        return 0;
    }
}
