package com.feedmejefferson.proxy;

import java.util.Random;

public class ActivePassiveManager {

    public static final long MAX_SESSION_ID = Integer.MAX_VALUE;
    private static final int POOL_SIZE = 4;
    private static final long SESSION_RANGE_SIZE = MAX_SESSION_ID / POOL_SIZE;
    private static long[] MIN_ROUTER_SESSION = new long[POOL_SIZE];
    private static long[] MAX_ROUTER_SESSION = new long[POOL_SIZE];
    {
        for (int i = 0; i < POOL_SIZE; i++) {
            MIN_ROUTER_SESSION[i] = i * SESSION_RANGE_SIZE;
            MAX_ROUTER_SESSION[i] = (i + 1) * SESSION_RANGE_SIZE - 1;
        }
    };

    private static Random random = new Random();
    private static int activeRouter = 0;
    private ModelRouter[] routers = new ModelRouter[POOL_SIZE];

    private static final ActivePassiveManager manager = new ActivePassiveManager();

    private ActivePassiveManager() {

    };

    public static ActivePassiveManager getActivePassiveManager() {
        return manager;
    }

    public ModelRouter getActive() {
        return routers[activeRouter];
    }

    public void newActive(ModelRouter router) {
        int nextActiveRouter = (activeRouter + 1) % POOL_SIZE;
        routers[nextActiveRouter] = router;
        activeRouter = nextActiveRouter;
    }

    public void suspendPassiveRouters() {
        for (ModelRouter router : routers) {
            if (router != null && router != routers[activeRouter]) {
                router.suspend();
            }
        }
    }

    public long getNewActiveSessionId() {
        // this only works as long as we limit our full
        // range of session ids to 31 bits
        return (random.nextInt((int) SESSION_RANGE_SIZE)
                + MIN_ROUTER_SESSION[activeRouter]);
    }

    public boolean isSessionRouterLive(long session) {
        int index = getSessionRouterIndex(session);
        return (routers[index] != null && routers[index].isLive());
    }

    public ModelRouter getSessionRouter(long session) {
        return (routers[getSessionRouterIndex(session)]);
    }

    private int getSessionRouterIndex(long session) {
        return (int) (session / SESSION_RANGE_SIZE);
    }

}
