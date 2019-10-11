package nextstep.mvc;

import nextstep.web.annotation.RequestMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class HandlerMethodArgumentResolverTest {
    private static final Logger logger = LoggerFactory.getLogger(HandlerMethodArgumentResolverTest.class);

    private ParameterNameDiscoverer nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    private HandlerMethodArgumentResolver resolver;

    @BeforeEach
    void setup() {
        resolver = HandlerMethodArgumentResolver.getInstance();
    }

    @Test
    void string() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String userId = "javajigi";
        String password = "password";
        request.addParameter("userId", userId);
        request.addParameter("password", password);

        Class clazz = TestUserController.class;
        Method method = getMethod("create_string", clazz.getDeclaredMethods());
        String[] parameterNames = nameDiscoverer.getParameterNames(method);
        Object[] values = resolver.resolve("", parameterNames, method.getParameters(), request,
                new MockHttpServletResponse());

        ModelAndView mav = (ModelAndView) method.invoke(clazz.newInstance(), values);
        assertThat(mav.getObject("userId")).isEqualTo(userId);
        assertThat(mav.getObject("password")).isEqualTo(password);
    }

    @Test
    void int_Integer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        int count = 2;
        Integer number = 10;
        request.addParameter("count", String.valueOf(count));
        request.addParameter("number", String.valueOf(number));

        Class clazz = TestUserController.class;
        Method method = getMethod("int_Integer", clazz.getDeclaredMethods());
        String[] parameterNames = nameDiscoverer.getParameterNames(method);
        Object[] values = resolver.resolve("", parameterNames, method.getParameters(), request,
                new MockHttpServletResponse());

        assertDoesNotThrow(() -> {
            ModelAndView mav = (ModelAndView) method.invoke(clazz.newInstance(), values);
            assertThat(mav.getObject("count")).isEqualTo(count);
            assertThat(mav.getObject("number")).isEqualTo(number);
        });
    }

    private Method getMethod(String name, Method[] methods) {
        return Arrays.stream(methods)
                .filter(method -> method.getName().equals(name))
                .findFirst()
                .get();
    }

    @Test
    void response_request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String userId = "javajigi";
        request.addParameter("userId", userId);

        Class clazz = TestUserController.class;
        Method method = getMethod("response_request", clazz.getDeclaredMethods());
        String[] parameterNames = nameDiscoverer.getParameterNames(method);

        Object[] params = resolver.resolve("", parameterNames, method.getParameters(), request, response);

        assertDoesNotThrow(() -> {
            ModelAndView mav = (ModelAndView) method.invoke(clazz.newInstance(), params);
            assertThat(mav.getObject("userId")).isEqualTo(userId);
        });
    }

    @Test
    void path_variable() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        long id = 33L;
        request.setRequestURI("/users/" + id);

        Class clazz = TestUserController.class;
        Method method = getMethod("show_pathvariable", clazz.getDeclaredMethods());
        String[] parameterNames = nameDiscoverer.getParameterNames(method);

        Object[] params = resolver.resolve(method.getAnnotation(RequestMapping.class).value(),
                parameterNames, method.getParameters(), request, response);

        assertDoesNotThrow(() -> {
            ModelAndView mav = (ModelAndView) method.invoke(clazz.newInstance(), params);
            assertThat(mav.getObject("id")).isEqualTo(id);
        });
    }
}
