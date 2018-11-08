package com.feedmejefferson.proxy;

import java.util.Collection;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.YAMLLibrary;

public class ConfigurationLoader extends RouteBuilder {

    private ActivePassiveManager manager = ActivePassiveManager
            .getActivePassiveManager();
    private String producerEndpoint;

    public ConfigurationLoader(String producerEndpoint) {
        this.producerEndpoint = producerEndpoint;
    }

    @Override
    public void configure() throws Exception {
        from(producerEndpoint).unmarshal().yaml(YAMLLibrary.SnakeYAML)
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        String fileName = (String) exchange.getIn()
                                .getHeader("CamelFileName");
                        if (fileName != null
                                && fileName.toLowerCase().contains("suspend")) {
                            manager.suspendPassiveRouters();
                        } else if (fileName != null && fileName.toLowerCase()
                                .contains("rollback")) {
                            // TODO implement rollback
                        } else {
                            Map config = (Map) (exchange.getMessage()
                                    .getBody());
                            String name = (String) config.get("name");
                            Collection models = (Collection) config
                                    .get("models");
                            ModelRouter router = new ModelRouter(name, models);
                            manager.newActive(router);
                        }

                    }
                });

    }

}
