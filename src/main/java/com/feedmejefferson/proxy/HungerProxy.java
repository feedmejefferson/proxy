package com.feedmejefferson.proxy;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

public class HungerProxy extends RouteBuilder {
    public static void main(String[] args) throws Exception {
        RouteBuilder builder = new HungerProxy();
        CamelContext ctx = builder.getContext();
        ctx.addRoutes(builder);
        ctx.start();
    }

    @Override
    public void configure() throws Exception {
        // just some sample code for playing around with multihop routes
        // from("netty4-http:http://localhost:8081/hunger.json").transform()
        // .constant("hello\n");
        // from("netty4-http:http://localhost:8082/hunger.json").transform()
        // .constant("world\n");
        // from("netty4-http:http://localhost:8083/hunger.json")
        // .to("netty4-http:http://localhost:8081/hunger.json");

        from("netty4-http:http://localhost:8080/hunger.json").choice()
                .when(header("searchSession").isNotNull())
                .to("netty4-http:http://localhost:9000/hunger.json").otherwise()
                .process(new AddSearchSession())
                .to("netty4-http:http://localhost:8080/hunger.json");

    }
}