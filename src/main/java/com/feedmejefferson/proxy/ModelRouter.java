package com.feedmejefferson.proxy;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.camel.builder.RouteBuilder;

public class ModelRouter {
    private String name;
    private boolean isLive = true;
    int totalWeight;
    NavigableMap<Integer, String> map = new TreeMap<Integer, String>();

    public class Builder extends RouteBuilder {

        @Override
        public void configure() throws Exception {
            // TODO Auto-generated method stub
        }
    }

    public ModelRouter(String name, Collection routes) {
        // TODO: this would all be so much easier with some jackson
        // annotations...
        this.name = name;
        for (Object route : routes) {
            if (route instanceof WeightedRoute) {
                map.put(totalWeight, ((WeightedRoute) route).getModelUrl());
                totalWeight += ((WeightedRoute) route).getWeight();
            } else if (route instanceof Map) {
                map.put(totalWeight, (String) ((Map) route).get("modelUrl"));
                totalWeight += Integer
                        .valueOf((Integer) ((Map) route).get("weight"));

            }
        }
    }

    public String getRoute(long session) {
        return map.floorEntry((int) (session % totalWeight)).getValue();
    }

    public void suspend() {
        isLive = false;
    }

    public boolean isLive() {
        return isLive;
    }

    public String getName() {
        return name;
    }
}
