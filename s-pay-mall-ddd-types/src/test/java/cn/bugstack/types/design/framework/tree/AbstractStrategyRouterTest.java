package cn.bugstack.types.design.framework.tree;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractStrategyRouterTest {

    @Test
    public void shouldRouteToSelectedHandler() throws Exception {
        TestRouter router = new TestRouter(true);

        assertEquals("selected", router.router("request", "context"));
    }

    @Test
    public void shouldFallbackToDefaultHandlerWhenMappingReturnsNull() throws Exception {
        TestRouter router = new TestRouter(false);
        router.setDefaultStrategyHandler((request, context) -> "default");

        assertEquals("default", router.router("request", "context"));
    }

    private static class TestRouter extends AbstractStrategyRouter<String, String, String> {

        private final boolean routeToHandler;

        TestRouter(boolean routeToHandler) {
            this.routeToHandler = routeToHandler;
        }

        @Override
        public StrategyHandler<String, String, String> get(String requestParameter, String dynamicContext) {
            return routeToHandler ? (request, context) -> "selected" : null;
        }

        @Override
        public String apply(String requestParameter, String dynamicContext) {
            return "router";
        }
    }
}
