package rniesler.gphotoshare.controllers;

import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import rniesler.gphotoshare.security.SecurityService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/oauth_login")
public class LoginController {
    private static String authorizationRequestBaseUri
            = "oauth2/authorization";
    Map<String, String> oauth2AuthenticationUrls
            = new HashMap<>();

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final SecurityService securityService;

    public LoginController(ClientRegistrationRepository clientRegistrationRepository, SecurityService securityService) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.securityService = securityService;
    }

    @GetMapping({"", "/"})
    public String login(Model model) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        } else {
            Iterable<ClientRegistration> clientRegistrations = null;
            ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository)
                    .as(Iterable.class);
            if (type != ResolvableType.NONE &&
                    ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
                clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
            }

            clientRegistrations.forEach(registration ->
                    oauth2AuthenticationUrls.put(registration.getClientName(),
                            authorizationRequestBaseUri + "/" + registration.getRegistrationId()));
            model.addAttribute("urls", oauth2AuthenticationUrls);

            return "login";
        }
    }
}
