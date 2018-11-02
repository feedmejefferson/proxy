package com.feedmejefferson.proxy;

import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class AddSearchSession implements Processor {

    private static final Random random = new Random();

    public void process(Exchange exchange) throws Exception {

        String header = String
                .valueOf(exchange.getIn().getHeader("CamelHttpQuery"));
        int searchSession = random.nextInt(Integer.MAX_VALUE);
        if (header == null) {
            header = "searchSession=" + searchSession;
        } else {
            header = "searchSession=" + searchSession + "&" + header;
        }
        exchange.getOut().setHeader("CamelHttpQuery", header);
    }

}
