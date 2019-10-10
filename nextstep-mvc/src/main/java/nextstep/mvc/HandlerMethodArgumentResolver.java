package nextstep.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class HandlerMethodArgumentResolver {

    private static HandlerMethodArgumentResolver instance;
    private static Map<Class<?>, Function<String, Object>> typeConverters;

    static {
        typeConverters = new HashMap<>();
        typeConverters.put(Byte.class, Byte::parseByte);
        typeConverters.put(Byte.TYPE, Byte::parseByte);
        typeConverters.put(Short.class, Short::parseShort);
        typeConverters.put(Short.TYPE, Short::parseShort);
        typeConverters.put(Integer.class, Integer::parseInt);
        typeConverters.put(Integer.TYPE, Integer::parseInt);
        typeConverters.put(Float.class, Float::parseFloat);
        typeConverters.put(Float.TYPE, Float::parseFloat);
        typeConverters.put(Double.class, Double::parseDouble);
        typeConverters.put(Double.TYPE, Double::parseDouble);
    }

    private HandlerMethodArgumentResolver() {
    }

    public static HandlerMethodArgumentResolver getInstance() {
        if (instance == null) {
            instance = new HandlerMethodArgumentResolver();
        }
        return instance;
    }

    public Object[] resolve(String[] parameterNames, Class<?>[] parameterTypes,
                            HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> requestParams = request.getParameterMap();

        return IntStream.range(0, parameterNames.length)
                .mapToObj(i -> findArgument(parameterNames[i], parameterTypes[i],
                        request, response, requestParams))
                .toArray();
    }

    private Object findArgument(String parameterName, Class<?> parameterType,
                                HttpServletRequest request, HttpServletResponse response,
                                Map<String, String[]> requestParams) {
        if (parameterType == HttpServletRequest.class) {
            return request;
        }
        if (parameterType == HttpServletResponse.class) {
            return response;
        }
        if (requestParams.containsKey(parameterName)) {
            return parsePrimitiveType(requestParams.get(parameterName)[0], parameterType);
        }
        return null;
    }

    private Object parsePrimitiveType(String value, Class<?> parameterType) {
        return Optional.ofNullable(typeConverters.get(parameterType))
                .orElse(v -> v)
                .apply(value);
    }
}
