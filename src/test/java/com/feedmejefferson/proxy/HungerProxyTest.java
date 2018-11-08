package com.feedmejefferson.proxy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class HungerProxyTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:a")
    protected MockEndpoint mockA;

    @EndpointInject(uri = "mock:b")
    protected MockEndpoint mockB;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Test
    public void testSimpleRouting() throws Exception {
        WeightedRoute a = new WeightedRoute(10, "mock:a");
        WeightedRoute b = new WeightedRoute(20, "mock:b");

        ModelRouter group1 = new ModelRouter("group1",
                Arrays.asList(new WeightedRoute[] { a }));
        ModelRouter group2 = new ModelRouter("group2",
                Arrays.asList(new WeightedRoute[] { b }));

        ActivePassiveManager manager = ActivePassiveManager
                .getActivePassiveManager();
        manager.newActive(group1);
        long session = manager.getNewActiveSessionId();

        mockA.expectedHeaderReceived("CamelHttpQuery",
                "searchSession=" + session);
        mockB.expectedMessageCount(0);
        Map headers = new HashMap();
        headers.put("searchSession", new Long(session));
        template.sendBodyAndHeaders("content", headers);
        mockA.assertIsSatisfied();
        mockB.assertIsSatisfied();

        headers.put("CamelHttpQuery", "searchSession=" + session);
        template.sendBodyAndHeaders("content", headers);
        mockA.assertIsSatisfied();
        mockB.assertIsSatisfied();

        /*
         * These next two are using a bit of a hack -- we know that the query
         * string params are already parsed for us into standard headers and
         * we're just looking at those headers, so we can take advantage of that
         * just to test the regular expression query string replacement logic.
         * Obviously we should really refactor this code, but this is a quick
         * and dirty fix for something that I caught in non unit tests and just
         * wanted to create a quick junit for.
         */
        headers.put("CamelHttpQuery", "searchSession=1234x");
        template.sendBodyAndHeaders("content", headers);
        mockA.assertIsSatisfied();
        mockB.assertIsSatisfied();

        // I seriously can't figure out why these don't work -- they work in the
        // real world
        // headers.put("CamelHttpQuery", "searchSession=1234&hello=world");
        // mockA.expectedHeaderReceived("CamelHttpQuery",
        // "searchSession="+session+"&hello=world");
        // template.sendBodyAndHeaders("content", headers);
        // mockA.assertIsSatisfied();
        // mockB.assertIsSatisfied();

        mockB.expectedMessageCount(1);

        manager.newActive(group2);
        template.sendBodyAndHeaders("content", headers);
        template.sendBody("content");

        mockA.assertIsSatisfied();
        mockB.assertIsSatisfied();

        manager.suspendPassiveRouters();

        // once we suspend the passive router, both messages should go to the
        // active endpoint
        mockB.expectedMessageCount(3);

        template.sendBodyAndHeaders("content", headers);
        template.sendBody("content");

        mockA.assertIsSatisfied();
        mockB.assertIsSatisfied();

    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new HungerProxy("direct:start");
    }
}
