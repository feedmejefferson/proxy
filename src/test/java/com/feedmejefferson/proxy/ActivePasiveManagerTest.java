package com.feedmejefferson.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class ActivePasiveManagerTest {

    @Test
    public void test() throws Exception {
        WeightedRoute a = new WeightedRoute(10, "a");
        WeightedRoute b = new WeightedRoute(20, "b");
        WeightedRoute c = new WeightedRoute(30, "c");

        ModelRouter group1 = new ModelRouter("group1",
                Arrays.asList(new WeightedRoute[] { a }));
        ModelRouter group2 = new ModelRouter("group2",
                Arrays.asList(new WeightedRoute[] { b }));
        ModelRouter group3 = new ModelRouter("group3",
                Arrays.asList(new WeightedRoute[] { c }));
        ModelRouter group4 = new ModelRouter("group4",
                Arrays.asList(new WeightedRoute[] { a, b }));
        ModelRouter group5 = new ModelRouter("group5",
                Arrays.asList(new WeightedRoute[] { a, b, c }));

        ActivePassiveManager manager = ActivePassiveManager
                .getActivePassiveManager();
        // we can't really assume that we're starting with a fresh system since
        // other tests may have already run that added routes to the manager
        // assertNull(manager.getActive());
        manager.newActive(group1);
        long session1a = manager.getNewActiveSessionId();
        long session1b = manager.getNewActiveSessionId();
        assertTrue(manager.isSessionRouterLive(session1a));
        assertTrue(manager.isSessionRouterLive(session1b));
        assertEquals(group1, manager.getActive());

        manager.newActive(group2);
        long session2a = manager.getNewActiveSessionId();
        long session2b = manager.getNewActiveSessionId();
        assertTrue(manager.isSessionRouterLive(session1a));
        assertTrue(manager.isSessionRouterLive(session1b));
        assertTrue(manager.isSessionRouterLive(session2a));
        assertTrue(manager.isSessionRouterLive(session2b));
        assertNotEquals(group1, manager.getActive());
        assertEquals(group2, manager.getActive());
        assertTrue(group1.isLive());
        manager.suspendPassiveRouters();
        assertFalse(group1.isLive());
        assertFalse(manager.isSessionRouterLive(session1a));
        assertFalse(manager.isSessionRouterLive(session1b));
        assertTrue(group2.isLive());
        assertTrue(manager.isSessionRouterLive(session2a));
        assertTrue(manager.isSessionRouterLive(session2b));

        manager.newActive(group3);
        long session3a = manager.getNewActiveSessionId();
        manager.newActive(group4);
        long session4a = manager.getNewActiveSessionId();
        assertEquals(group3, manager.getSessionRouter(session3a));
        assertEquals(group4, manager.getSessionRouter(session4a));
        manager.suspendPassiveRouters();
        assertFalse(manager.isSessionRouterLive(session2a));
        assertFalse(manager.isSessionRouterLive(session3a));
        assertTrue(manager.isSessionRouterLive(session4a));

        manager.newActive(group5);
        assertFalse(manager.isSessionRouterLive(session2a));
        assertFalse(manager.isSessionRouterLive(session3a));
        assertTrue(manager.isSessionRouterLive(session4a));

    }

}
