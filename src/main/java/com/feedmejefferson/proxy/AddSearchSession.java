package com.feedmejefferson.proxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class AddSearchSession implements Processor {

    private static final ActivePassiveManager manager = ActivePassiveManager
            .getActivePassiveManager();
    private static final Pattern SESSION_PATTERN = Pattern
            .compile("searchSession=[^&]*");

    public void process(Exchange exchange) throws Exception {

        long session;
        Object sessionOb = exchange.getIn().getHeader("searchSession");
        if (sessionOb instanceof Long
                && manager.isSessionRouterLive((Long) sessionOb)) {
            session = (Long) sessionOb;
        } else if (sessionOb instanceof String && manager
                .isSessionRouterLive(Long.valueOf((String) sessionOb))) {
            session = Long.valueOf((String) sessionOb);
        } else {
            session = manager.getNewActiveSessionId();
        }
        String model = manager.getSessionRouter(session).getRoute(session);

        String header = (String) exchange.getIn().getHeader("CamelHttpQuery");
        String searchParam = "searchSession=" + session;
        if (header == null) {
            header = searchParam;
        } else {
            Matcher m = SESSION_PATTERN.matcher(header);
            if (m.find()) {
                header = m.replaceAll(searchParam);
            } else {
                header = searchParam + "&" + header;
            }
        }
        exchange.getOut().setHeader("CamelHttpQuery", header);
        exchange.getOut().setHeader("model", model);
    }

}
