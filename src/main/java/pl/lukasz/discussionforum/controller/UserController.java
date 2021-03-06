package pl.lukasz.discussionforum.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.lukasz.discussionforum.entity.Role;
import pl.lukasz.discussionforum.entity.User;
import pl.lukasz.discussionforum.service.RoleService;
import pl.lukasz.discussionforum.service.ThreadService;
import pl.lukasz.discussionforum.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class UserController {

    private UserService userService;
    private RoleService roleService;
    private ThreadService threadService;

    public UserController(UserService userService, RoleService roleService, ThreadService threadService) {
        this.userService = userService;
        this.roleService = roleService;
        this.threadService = threadService;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Authentication authentication)
    {
        if(authentication != null)
        {
            return "redirect:/";
        }
        else {
            return "login";
        }
    }

    @RequestMapping(value ="/register", method = RequestMethod.GET)
    public String registerNewUser(Model model, Authentication authentication) {
        if(authentication != null) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "forms/registerForm";
    }

    @RequestMapping(value ="/register", method = RequestMethod.POST)
    public String saveNewUser(@Valid @ModelAttribute("user") User user, BindingResult result) {
        if(result.hasErrors()) {
            return "forms/registerForm";
        }
        userService.presave(user);
        return "redirect:/login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(Authentication authentication, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if(authentication != null)
        {
            new SecurityContextLogoutHandler().logout(httpServletRequest,httpServletResponse,authentication);
        }

        return "redirect:/";
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getAllUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @RequestMapping(value = "/users/delete/{id}", method = RequestMethod.GET)
    public String deleteUser(@PathVariable("id") Long id) {
        userService.delete(id);
        return "redirect:/users";
    }

    @RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
    public String findOneUserById(@PathVariable("username") String username, Model model) {
        model.addAttribute("user", userService.findByUsername(username));
        model.addAttribute("threads", threadService.findAllAndOrderByCreationType());
        return "user";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(Authentication authentication, Model model) {
        User user;
        user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "user";

    }

    @RequestMapping(value = "/user/{userId}/edit", method = RequestMethod.GET)
    public String editUser(@PathVariable("userId") Long userId, Authentication authentication, Model model) {
        if(authentication.getName().equals(userService.findOneUserById(userId).get().getUsername())
                || userService.findByUsername(authentication.getName()).getRoles().contains(roleService.findByName("ADMIN"))) {

            User user = userService.findOneUserById(userId).get();
            model.addAttribute("editUser", user);
            return "forms/editUserForm";
        }
        return "redirect:/";

    }

    @RequestMapping(value = "/user/{userId}/edit", method = RequestMethod.POST)
    public String saveEditedUser( @PathVariable("userId") Long userId,@Valid @ModelAttribute("editUser") User user, BindingResult bindingResult, Authentication authentication,HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if(bindingResult.hasErrors()) {
            return "forms/editUserForm";
        }

            userService.presaveEdited(user);
            logout(authentication, httpServletRequest, httpServletResponse);
            return "redirect:/login";

    }

}
