package rniesler.gphotoshare.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.admin.SecurityMapping;
import rniesler.gphotoshare.security.Authorities;
import rniesler.gphotoshare.services.AccessManagementService;

import javax.validation.Valid;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AccessManagementService accessManagementService;

    public AdminController(AccessManagementService accessManagementService) {
        this.accessManagementService = accessManagementService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("newuser", new SecurityMapping());
        return setListUsersModel(model);
    }

    private String setListUsersModel(Model model) {
        model.addAttribute("users", accessManagementService.listAllowedUsers());
        model.addAttribute("requests", accessManagementService.listPendingAccessRequests());
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
            accessManagementService.saveMapping(newSecurityMapping);
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{email}/delete")
    public String deleteUser(@PathVariable("email") String email) {
        accessManagementService.deleteMapping(email);
        return "redirect:/admin/users";
    }

    @PostMapping("/accessRequest/{email}/grant")
    public String grantAccessRequest(@PathVariable("email") String email) {
        accessManagementService.grantAccessRequest(email);
        return "redirect:/admin/users";

    }

    @PostMapping("/accessRequest/{email}/deny")
    public String denyAccessRequest(@PathVariable("email") String email) {
        accessManagementService.denyAccessRequest(email);
        return "redirect:/admin/users";
    }
}
