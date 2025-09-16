package org.mec.aeronlab.archive;

import jakarta.inject.Inject;

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class SequencePositionTracker
{
    private final ConcurrentNavigableMap<Long, Long> seqToPosition = new ConcurrentSkipListMap<>();

    @Inject
    public SequencePositionTracker()
    {
        // Do nothing.
    }

    public void record(long sequenceNumber, long position)
    {
        seqToPosition.put(sequenceNumber, position);
    }

    public Long lookupPosition(long sequenceNumber)
    {
        return seqToPosition.get(sequenceNumber);
    }

    public Long lookupClosestPosition(long sequenceNumber)
    {
        return seqToPosition.floorEntry(sequenceNumber).getValue();
    }
}
