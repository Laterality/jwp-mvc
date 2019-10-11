package nextstep.mvc;

import nextstep.web.annotation.PathVariable;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class HandlerMethodArgumentResolver {

    private static HandlerMethodArgumentResolver instance;
    private static Map<Class<?>, Function<String, Object>> typeConverters;
    private static PathPatternParser pathPatternParser;

    static {
        pathPatternParser = new PathPatternParser();

        typeConverters = new HashMap<>();
        typeConverters.put(Byte.class, Byte::parseByte);
        typeConverters.put(byte.class, Byte::parseByte);
        typeConverters.put(Short.class, Short::parseShort);
        typeConverters.put(short.class, Short::parseShort);
        typeConverters.put(Integer.class, Integer::parseInt);
        typeConverters.put(int.class, Integer::parseInt);
        typeConverters.put(Long.class, Long::parseLong);
        typeConverters.put(long.class, Long::parseLong);
        typeConverters.put(Float.class, Float::parseFloat);
        typeConverters.put(float.class, Float::parseFloat);
        typeConverters.put(Double.class, Double::parseDouble);
        typeConverters.put(double.class, Double::parseDouble);
    }

    private HandlerMethodArgumentResolver() {
    }

    public static HandlerMethodArgumentResolver getInstance() {
        if (instance == null) {
            instance = new HandlerMethodArgumentResolver();
        }
        return instance;
    }

    public Object[] resolve(String requestMappingURI, String[] parameterNames, Parameter[] parameters,
                            HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> requestParams = request.getParameterMap();

        return IntStream.range(0, parameterNames.length)
                .mapToObj(i -> findArgument(requestMappingURI, parameterNames[i], parameters[i],
                        request, response, requestParams))
                .toArray();
    }

    private Object findArgument(String requestMappingURI, String parameterName, Parameter parameter,
                                HttpServletRequest request, HttpServletResponse response,
                                Map<String, String[]> requestParams) {
        if (parameter.getType() == HttpServletRequest.class) {
            return request;
        }
        if (parameter.getType() == HttpServletResponse.class) {
            return response;
        }
        if (parameter.isAnnotationPresent(PathVariable.class)) {
            return parsePathVariable(requestMappingURI, parameterName, parameter, request);
        }
        if (requestParams.containsKey(parameterName)) {
            return parsePrimitiveType(requestParams.get(parameterName)[0], parameter.getType());
        }
        return null;
    }

    private Object parsePathVariable(String requestMappingURI, String parameterName, Parameter parameter, HttpServletRequest request) {
        Map<String, String> pathVariables = pathPatternParser.parse(requestMappingURI)
                .matchAndExtract(toPathContainer(request.getRequestURI())).getUriVariables();
        String pathVariableName = getPathVariableName(parameterName, parameter);
        return wrapOptionalIfNecessary(parameter, pathVariables, pathVariableName);
    }

    private static PathContainer toPathContainer(String path) {
        if (path == null) {
            return null;
        }
        return PathContainer.parsePath(path);
    }

    private String getPathVariableName(String parameterName, Parameter parameter) {
        String pathVariableName = parameter.getAnnotation(PathVariable.class).name();
        if (pathVariableName.isEmpty()) {
            pathVariableName = parameterName;
        }
        return pathVariableName;
    }

    private Object wrapOptionalIfNecessary(Parameter parameter, Map<String, String> pathVariables, String pathVariableName) {
        Object param = parsePrimitiveType(pathVariables.get(pathVariableName), parameter.getType());
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (parameter.getType() == Optional.class && pathVariable.required()) {
            param = Optional.ofNullable(param);
        }
        return param;
    }

    private Object parsePrimitiveType(String value, Class<?> parameterType) {
        return Optional.ofNullable(typeConverters.get(parameterType))
                .orElse(v -> v)
                .apply(value);
    }
}
