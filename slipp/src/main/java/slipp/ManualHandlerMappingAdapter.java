package slipp;

import nextstep.mvc.HandlerMapping;
import nextstep.mvc.asis.Controller;
import nextstep.mvc.tobe.HandlerExecution;
import nextstep.mvc.tobe.JspView;
import nextstep.mvc.tobe.ModelAndView;
import nextstep.mvc.tobe.RedirectView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class ManualHandlerMappingAdapter implements HandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(ManualHandlerMappingAdapter.class);
    private static final String REDIRECT_VIEW_PREFIX = "redirect:";
    private final ManualHandlerMapping handlerMapping;

    public ManualHandlerMappingAdapter(ManualHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void initialize() {
        handlerMapping.initialize();
    }

    @Override
    public HandlerExecution getHandler(HttpServletRequest request) {
        Controller controller = handlerMapping.getHandler(request);
        if (controller == null) {
            return null;
        }

        return (req, resp) -> {
            try {
                String viewName = controller.execute(req, resp);
                return getModelAndView(viewName);
            } catch (Exception e) {
                logger.error("Failed to getting handler", e);
                throw new RuntimeException(e);
            }
        };
    }

    private ModelAndView getModelAndView(String viewName) {
        if (viewName.startsWith(REDIRECT_VIEW_PREFIX)) {
            return new ModelAndView(new RedirectView(viewName.substring(REDIRECT_VIEW_PREFIX.length())));
        }
        return new ModelAndView(new JspView(viewName));
    }
}
