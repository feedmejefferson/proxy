package com.feedmejefferson.proxy;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ConfigurationLoaderTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    private ActivePassiveManager manager = ActivePassiveManager
            .getActivePassiveManager();

    @Test
    public void testConfiguration() throws Exception {
        String yaml1 = "name: configured1\n" + "models:\n"
                + "-   modelUrl: mock:a\n" + "    weight: 10\n"
                + "-   modelUrl: mock:b\n" + "    weight: 5\n";
        String yaml2 = "name: configured2\n" + "models:\n"
                + "-   modelUrl: mock:c\n" + "    weight: 10\n"
                + "-   modelUrl: mock:d\n" + "    weight: 5\n";

        template.sendBody(yaml1);

        ModelRouter router = manager.getActive();
        long session = manager.getNewActiveSessionId();
        String url = manager.getSessionRouter(session).getRoute(session);
        assertEquals("configured1", router.getName());

        // after we send a new configuration, the original router
        // should no longer be active, but it should still route old sessions
        // to the same urls
        template.sendBody(yaml2);
        assertEquals("configured2", manager.getActive().getName());
        assertTrue(manager.isSessionRouterLive(session));
        assertEquals(url, manager.getSessionRouter(session).getRoute(session));

    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new ConfigurationLoader("direct:start");

    }
}
