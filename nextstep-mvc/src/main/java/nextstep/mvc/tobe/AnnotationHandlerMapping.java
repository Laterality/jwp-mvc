package nextstep.mvc.tobe;

import com.google.common.collect.Maps;
import nextstep.mvc.HandlerMapping;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.annotation.RequestMethod;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private Object[] basePackage;

    private Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();

    public AnnotationHandlerMapping(Object... basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void initialize() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        logger.info("{} Controllers are added by Annotation.", controllers.size());
        controllers.stream()
                .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(RequestMapping.class)))
                .forEach(this::mapControllerMethod);
    }

    private void mapControllerMethod(Method method) {
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        RequestMethod[] methods = mapping.method();
        if (mapping.method().length == 0) {
            methods = RequestMethod.values();
        }
        Arrays.stream(methods)
                .map(requestMethod -> new HandlerKey(mapping.value(), requestMethod))
                .forEach(key -> handlerExecutions.put(key, executeController(method, getInstanceFromMethod(method))));
    }

    private Object getInstanceFromMethod(Method method) {
        try {
            return method.getDeclaringClass().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Error while registering controller methods", e);
            throw new HandlerMappingException(e);
        }
    }

    private HandlerExecution executeController(Method method, Object instance) {
        return (request, response) -> {
            try {
                return (ModelAndView) method.invoke(instance, request, response);
            } catch (InvocationTargetException | IllegalAccessException e) {
                logger.error("Error occurred while handle request", e);
                throw new HandlerMappingException(e);
            }
        };
    }

    @Override
    public HandlerExecution getHandler(HttpServletRequest request) {
        return handlerExecutions.get(new HandlerKey(request.getRequestURI(), RequestMethod.valueOf(request.getMethod())));
    }
}
