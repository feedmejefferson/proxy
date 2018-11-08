package com.feedmejefferson.proxy;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class HungerProxy extends RouteBuilder {

    String producerEndpoint;
    Processor searchSessionProcessor = new AddSearchSession();

    public static void main(String[] args) throws Exception {
        RouteBuilder proxy = new HungerProxy(
                "netty4-http:http://localhost:8080/hunger.json");
        RouteBuilder configLoader = new ConfigurationLoader("file:configs");
        CamelContext ctx = proxy.getContext();
        ctx.addRoutes(proxy);
        ctx.addRoutes(configLoader);
        ctx.start();
    }

    public HungerProxy(String producerEndpoint) {
        this.producerEndpoint = producerEndpoint;

    }

    @Override
    public void configure() throws Exception {
        // swallow exceptions and log them before they make it back in the
        // response
        onException(Exception.class).handled(true)
                .to("log:com.feedmejefferson.proxy?level=ERROR");

        from(producerEndpoint).process(searchSessionProcessor)
                .routingSlip(header("model"));

    }

}