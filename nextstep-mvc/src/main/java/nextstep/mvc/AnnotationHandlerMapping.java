package nextstep.mvc;

import com.google.common.collect.Maps;
import nextstep.mvc.exception.HandlerMappingException;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.annotation.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private RequestMappingScanner requestMappingScanner;
    private Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();
    private HandlerMethodArgumentResolver argumentResolver;
    private ParameterNameDiscoverer nameDiscoverer;

    public AnnotationHandlerMapping(Object... basePackage) {
        this.requestMappingScanner = new RequestMappingScanner(ComponentScanner.of(basePackage));
        argumentResolver = HandlerMethodArgumentResolver.getInstance();
        nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    }

    @Override
    public void initialize() {
        Set<Method> methods = requestMappingScanner.getRequestMappingMethods();
        logger.info("{} Controllers are added by Annotation.", methods.size());
        methods.forEach(this::mapControllerMethod);
    }

    private void mapControllerMethod(Method method) {
        PathPatternParser pathPatternParser = new PathPatternParser();
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        RequestMethod[] methods = mapping.method();
        if (mapping.method().length == 0) {
            methods = RequestMethod.values();
        }
        Arrays.stream(methods)
                .map(requestMethod -> new HandlerKey(pathPatternParser.parse(mapping.value()), requestMethod))
                .forEach(key -> handlerExecutions.put(key, executeController(method, Instances.getFromMethod(method))));
    }

    private HandlerExecution executeController(Method method, Object instance) {
        return (request, response) -> {
            try {
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                return (ModelAndView) method.invoke(instance,
                        argumentResolver.resolve(
                                requestMapping.value(),
                                nameDiscoverer.getParameterNames(method),
                                method.getParameters(),
                                request, response));
            } catch (InvocationTargetException | IllegalAccessException e) {
                logger.error("Error occurred while handle request", e);
                throw new HandlerMappingException(e);
            }
        };
    }

    @Override
    public Optional<HandlerExecution> getHandler(HttpServletRequest request) {
        return handlerExecutions.entrySet().stream()
                .filter(entry -> entry.getKey().matchPattern(request.getRequestURI()))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
