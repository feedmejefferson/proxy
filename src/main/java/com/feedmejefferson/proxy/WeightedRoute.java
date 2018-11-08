package com.feedmejefferson.proxy;

public class WeightedRoute {

    private int weight;
    private String modelUrl;

    public int getWeight() {
        return weight;
    }

    public String getModelUrl() {
        return modelUrl;
    }

    public WeightedRoute() {
    }

    public WeightedRoute(int weight, String modelUrl) {
        this.weight = weight;
        this.modelUrl = modelUrl;
    }
}
