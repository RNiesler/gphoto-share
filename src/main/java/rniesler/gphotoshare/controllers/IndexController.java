package rniesler.gphotoshare.controllers;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
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
        Mono<OAuth2AuthorizedClient> authorizedClient = securityService.getAuthorizedClient(authentication);
        model.addAttribute("userName", authentication.getName());
        model.addAttribute("clientName", authorizedClient.map(OAuth2AuthorizedClient::getClientRegistration)
                .map(ClientRegistration::getClientName));
        model.addAttribute("albums", viewerService.retrieveAccessibleAlbums(authentication));
        return "index";
    }

    @RequestMapping("/userinfo")
    public String userinfo(Model model, OAuth2AuthenticationToken authentication) {
        Mono<Map> userAttributes = securityService.retrieveUserInfo(authentication);
        model.addAttribute("userAttributes", userAttributes);
        return "userinfo";
    }
}