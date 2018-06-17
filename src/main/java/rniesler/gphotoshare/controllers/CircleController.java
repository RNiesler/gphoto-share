package rniesler.gphotoshare.controllers;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.CircleService;

import java.util.LinkedList;

@Controller
@RequestMapping("/circles")
public class CircleController {
    private final CircleService circleService;
    private final SecurityService securityService;

    public CircleController(CircleService circleService, SecurityService securityService) {
        this.circleService = circleService;
        this.securityService = securityService;
    }

    @GetMapping({"", "/"})
    public String listCircles(Model model, OAuth2AuthenticationToken authentication) {
        model.addAttribute("circles", circleService.findAll(authentication));
        model.addAttribute("newcircle", new Circle());
        return "circles";
    }

    @PostMapping({"", "/"})
    public String newCircle(@ModelAttribute Circle circle, OAuth2AuthenticationToken authentication) {
        circle.setOwner(securityService.getAuthenticatedEmail(authentication));
        circleService.persist(circle).block();
        return "redirect:/circles";
    }

    @GetMapping("/{id}")
    public String getCircle(@PathVariable("id") String id, Model model, OAuth2AuthenticationToken authentication) {
        model.addAttribute("circle", circleService.get(id));
        model.addAttribute("newMemberCommand", new Person());
        return "circle";
    }

    @PostMapping("/{id}")
    public String updateMembers(@PathVariable("id") String id, @ModelAttribute Circle circleCommand, OAuth2AuthenticationToken authentication) {
        circleService.get(id)
                .doOnNext(circle -> circle.setMembers(circleCommand.getMembers()))
                .flatMap(circle -> circleService.persist(circle))
                .block();
        return "redirect:/circles/" + id;

    }

    @PostMapping("/{id}/members/{member}")
    public String removeMember(@PathVariable("id") String circleId, @PathVariable("member") String memberEmail) {
        circleService.get(circleId)
                .doOnNext(circle -> circle.getMembers().remove(memberEmail))
                .flatMap(circle -> circleService.persist(circle))
                .block();
        return "redirect:/circles/" + circleId;
    }

    @PostMapping("/{id}/members")
    public String addMember(@PathVariable("id") String circleId, @ModelAttribute Person newMember) {
        circleService.get(circleId)
                .doOnNext(circle -> {
                    if (circle.getMembers() == null) {
                        circle.setMembers(new LinkedList<>());
                    }
                    circle.getMembers().add(newMember.getEmail());
                })
                .flatMap(circle -> circleService.persist(circle))
                .block();
        return "redirect:/circles/" + circleId;
    }
    //TODO import circles from G+ (w || wo resetting)
}
