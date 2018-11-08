package com.feedmejefferson.proxy;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class ModelRouterTest {

    @Test
    public void test() throws Exception {
        WeightedRoute a = new WeightedRoute(10, "a");
        WeightedRoute b = new WeightedRoute(5, "b");

        ModelRouter router = new ModelRouter("group1",
                Arrays.asList(new WeightedRoute[] { a, b }));
        assertEquals(router.getRoute(5), "a");
        assertEquals(router.getRoute(11), "b");
        assertEquals(router.getRoute(31), "a");
    }

}
