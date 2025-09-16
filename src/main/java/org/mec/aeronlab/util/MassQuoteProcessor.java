package org.mec.aeronlab.util;

import org.mec.aeronlab.MassQuoteProto;

import java.util.List;

public class MassQuoteProcessor {

    public static byte[] encodeMassQuote(int i) {
        var quote1 = MassQuoteProto.QuoteEntry.newBuilder()
                .setSymbol("APPL")
                .setBidPrice(189.50)
                .setAskPrice(190.05)
                .setBidSize(100)
                .setAskSize(120)
                .setQuoteCondition("Firm")
                .build();

        var quote2 = MassQuoteProto.QuoteEntry.newBuilder()
                .setSymbol("GOOG")
                .setBidPrice(2750.05)
                .setAskPrice(2760.05)
                .setBidSize(50)
                .setAskSize(60)
                .setQuoteCondition("Indicative")
                .build();

        var massQuote = MassQuoteProto.MassQuote.newBuilder()
                .setQuoteId(String.valueOf(i))
                .setQuoteReqId("REQ67898")
                .setQuoteType("Tradable")
                .setQuoteStatus("AcceptedW")
                .addAllEntries(List.of(quote1, quote2))
                .build();

        return massQuote.toByteArray();
    }

    public static MassQuoteProto.MassQuote decodeMassQuote(byte[] data) throws Exception {
        var decoded = MassQuoteProto.MassQuote.parseFrom(data);

        System.out.println("Quote ID: " + decoded.getQuoteId());

        for (var entry : decoded.getEntriesList()) {
            System.out.println("""    
                    Symbol: %s
                    Bid: %.2f (%s)
                    Ask: %.2f (%s)
                    Condition: %s
                    """.formatted(
                    entry.getSymbol(),
                    entry.getBidPrice(), entry.getBidSize(),
                    entry.getAskPrice(), entry.getAskSize(),
                    entry.getQuoteCondition()));
        }

        return decoded;
    }

    //----- test
    public static void main(String[] args) {
        var encoded = encodeMassQuote(123);
        try
        {
            MassQuoteProto.MassQuote mq = decodeMassQuote(encoded);
        }
        catch (Exception e)
        {
            System.out.println("Exception while decoding MassQuote. error=" + e);
            Thread.currentThread().interrupt();
        }
    }
}
