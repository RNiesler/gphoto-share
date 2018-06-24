package rniesler.gphotoshare.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.security.Authorities;
import rniesler.gphotoshare.services.SecurityMappingService;

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
        model.addAttribute("users", securityMappingService.listAllowedUsers());
        model.addAttribute("newuser", new SecurityMapping());
        return "listusers";
    }

    @PostMapping("/users")
    public String addUser(@ModelAttribute SecurityMapping newSecurityMapping) {
        if (newSecurityMapping.getAuthorities() == null) {
            newSecurityMapping.setAuthorities(Set.of(Authorities.RNALLOWED.name()));
        }
        securityMappingService.saveMapping(newSecurityMapping);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{email}/delete")
    public String deleteUser(@PathVariable("email") String email) {
        securityMappingService.deleteMapping(email);
        return "redirect:/admin/users";
    }
}
