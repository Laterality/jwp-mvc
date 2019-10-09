package nextstep.mvc;

public class HandlerMethodArgumentResolver {

    public HandlerMethodArgumentResolver() {

    }


    public Object[] resolve(String[] parameterNames, Class<?>[] types) {
        assertSizeMatch(parameterNames, types);

        return new Object[]{};
    }

    private void assertSizeMatch(String[] parameterNames, Class<?>[] types) {
        if (parameterNames.length != types.length) {
            throw new HandlerMethodArgumentResolverException("Lengths of arrays of parameters and types are not equal");
        }
    }

    private class HandlerMethodArgumentResolverException extends RuntimeException {
        public HandlerMethodArgumentResolverException(String message) {
            super(message);
        }
    }
}
