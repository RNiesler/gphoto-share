package rniesler.gphotoshare.controllers;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.ViewerService;

@Controller
public class IndexController {
    private final SecurityService securityService;
    private final ViewerService viewerService;

    public IndexController(SecurityService securityService, ViewerService viewerService) {
        this.securityService = securityService;
        this.viewerService = viewerService;
    }

    @GetMapping("/")
    public String index(Model model) {
        OAuth2AuthenticationToken authenticationToken = securityService.retrieveAuthenticationToken();
        OAuth2AuthorizedClient authorizedClient = securityService.getAuthorizedClient(authenticationToken);
        model.addAttribute("userName", authenticationToken.getName());
        model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
        model.addAttribute("albums", viewerService.retrieveAccessibleAlbums());
        return "index";
    }
}