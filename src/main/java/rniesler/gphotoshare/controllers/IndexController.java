package rniesler.gphotoshare.controllers;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.ViewerService;

import java.util.Map;

@Controller
public class IndexController {
    private final SecurityService securityService;
    private final ViewerService viewerService;

    public IndexController(SecurityService securityService, ViewerService viewerService) {
        this.securityService = securityService;
        this.viewerService = viewerService;
    }

    @GetMapping("/")
    public String index(Model model, OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient authorizedClient = securityService.getAuthorizedClient(authentication);
        model.addAttribute("userName", authentication.getName());
        model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
        model.addAttribute("albums", viewerService.retrieveAccessibleAlbums());
        return "index";
    }

    @RequestMapping("/userinfo")
    public String userinfo(Model model, OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient authorizedClient = securityService.getAuthorizedClient(authentication);
        Map userAttributes = securityService.retrieveUserInfo(authentication);
        model.addAttribute("userAttributes", userAttributes);
        return "userinfo";
    }
}