package rniesler.gphotoshare.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.commands.RequestAccessCommand;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.AccessManagementService;

import javax.validation.Valid;

@Controller
@RequestMapping("/errors")
public class ErrorController {
    private final SecurityService securityService;
    private final AccessManagementService accessManagementService;

    public ErrorController(SecurityService securityService, AccessManagementService accessManagementService) {
        this.securityService = securityService;
        this.accessManagementService = accessManagementService;
    }

    @GetMapping("/accessDenied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied(Model model) {
        return setModelForAccessDenied(model);
    }

    private String setModelForAccessDenied(Model model) {
        if (securityService.isAuthenticated() &&
                !accessManagementService.isUserAllowed(securityService.getAuthenticatedEmail())) {
            String email = securityService.getAuthenticatedEmail();
            accessManagementService.getAccessRequest(email).ifPresentOrElse(accessRequest -> {
                model.addAttribute("existingRequest", accessRequest);
            }, () -> {
                if (!model.containsAttribute("request")) {
                    model.addAttribute("request", RequestAccessCommand.builder().email(email).build());
                }
            });
        }
        return "errors/accessDenied";
    }

    @PostMapping("/requestAccess")
    public String requestAccess(@Valid @ModelAttribute("request") RequestAccessCommand requestAccessCommand,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return setModelForAccessDenied(model);
        } else {
            accessManagementService.requestAccess(requestAccessCommand);
            return "redirect:/errors/accessDenied";
        }

    }
}
