package org.mec.chroniclelab.structuredmessaging;

import net.openhft.chronicle.wire.SelfDescribingMarshallable;

import java.util.concurrent.ThreadLocalRandom;

public class MarketData extends SelfDescribingMarshallable
{
    int secId;
    long time;
    float last;
    float high;
    float low;

    public MarketData()
    {
        super();
    }

    public static MarketData create()
    {
        MarketData marketData = new MarketData();
        float nextFloat = ThreadLocalRandom.current().nextFloat();
        float last = 20 + 100 * nextFloat;

//        marketData.setSecId(ThreadLocalRandom.current().nextInt(1_000));
        marketData.setSecId(0);
        marketData.setLast(last);
        marketData.setHigh(last * 1.1f);
        marketData.setLow(last * 0.9f);
        marketData.setTime(System.currentTimeMillis());

        return marketData;
    }

    static MarketData recycle(MarketData marketData)
    {
        final int id = ThreadLocalRandom.current().nextInt(1000);
        marketData.setSecId(marketData.secId + 1);
        final float nextFloat = ThreadLocalRandom.current().nextFloat();
        final float last = 20 + 100 * nextFloat;

        marketData.setLast(last);
        marketData.setHigh(last * 1.1f);
        marketData.setLow(last * 0.9f);
        marketData.setTime(System.currentTimeMillis());

        return marketData;
    }

    public int getSecId()
    {
        return secId;
    }

    public void setSecId(int secId)
    {
        this.secId = secId;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public float getLast()
    {
        return last;
    }

    public void setLast(float last)
    {
        this.last = last;
    }

    public float getHigh()
    {
        return high;
    }

    public void setHigh(float high)
    {
        this.high = high;
    }

    public float getLow()
    {
        return low;
    }

    public void setLow(float low)
    {
        this.low = low;
    }
}

