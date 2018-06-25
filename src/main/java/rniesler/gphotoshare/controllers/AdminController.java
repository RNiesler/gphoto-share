package rniesler.gphotoshare.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.security.Authorities;
import rniesler.gphotoshare.services.SecurityMappingService;

import javax.validation.Valid;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final SecurityMappingService securityMappingService;

    public AdminController(SecurityMappingService securityMappingService) {
        this.securityMappingService = securityMappingService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("newuser", new SecurityMapping());
        return setListUsersModel(model);
    }

    private String setListUsersModel(Model model) {
        model.addAttribute("users", securityMappingService.listAllowedUsers());
        return "listusers";
    }

    @PostMapping("/users")
    public String addUser(@Valid @ModelAttribute("newuser") SecurityMapping newSecurityMapping,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return setListUsersModel(model);
        } else {
            if (newSecurityMapping.getAuthorities() == null) {
                newSecurityMapping.setAuthorities(Set.of(Authorities.RNALLOWED.name()));
            }
            securityMappingService.saveMapping(newSecurityMapping);
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{email}/delete")
    public String deleteUser(@PathVariable("email") String email) {
        securityMappingService.deleteMapping(email);
        return "redirect:/admin/users";
    }
}
