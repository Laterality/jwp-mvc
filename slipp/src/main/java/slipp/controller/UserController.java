package slipp.controller;

import nextstep.mvc.JspView;
import nextstep.mvc.ModelAndView;
import nextstep.mvc.RedirectView;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.annotation.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slipp.controller.exception.IllegalRequestException;
import slipp.controller.exception.UserNotFoundException;
import slipp.domain.User;
import slipp.support.db.DataBase;

import javax.servlet.http.HttpServletRequest;

@Controller
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @RequestMapping(value = "/users/create", method = RequestMethod.POST)
    public ModelAndView create(String userId, String password, String name, String email) {
        User user = new User(userId, password, name, email);
        log.debug("User : {}", user);

        DataBase.addUser(user);
        return new ModelAndView(new RedirectView("/"));
    }

    @RequestMapping(value = "/users/profile", method = RequestMethod.GET)
    public ModelAndView profile(HttpServletRequest req, String userId) {
        User user = DataBase.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        req.setAttribute("user", user);
        return new ModelAndView(new JspView("/user/profile.jsp"));
    }

    @RequestMapping(value = "/users/updateForm", method = RequestMethod.GET)
    public ModelAndView showUpdateForm(HttpServletRequest req, String userId) {
        User user = DataBase.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (!UserSessionUtils.isSameUser(req.getSession(), user)) {
            throw new IllegalRequestException("다른 사용자의 정보를 수정할 수 없습니다.");
        }
        req.setAttribute("user", user);
        return new ModelAndView(new JspView("/user/updateForm.jsp"));
    }

    @RequestMapping(value = "/users/update", method = RequestMethod.POST)
    public ModelAndView updateUser(HttpServletRequest req, String userId, String password,
                                   String name, String email) {
        User user = DataBase.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (!UserSessionUtils.isSameUser(req.getSession(), user)) {
            throw new IllegalRequestException("다른 사용자의 정보를 수정할 수 없습니다.");
        }

        User updateUser = new User(userId, password, name, email);
        log.debug("Update User : {}", updateUser);
        user.update(updateUser);
        return new ModelAndView(new RedirectView("/"));
    }
}
