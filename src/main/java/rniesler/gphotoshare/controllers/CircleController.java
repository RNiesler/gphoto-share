package rniesler.gphotoshare.controllers;

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
    public String listCircles(Model model) {
        model.addAttribute("circles", circleService.findAll());
        model.addAttribute("newcircle", new Circle());
        return "circles";
    }

    @PostMapping({"", "/"})
    public String newCircle(@ModelAttribute Circle circle) {
        circle.setOwner(securityService.getAuthenticatedEmail());
        circleService.persist(circle);
        return "redirect:/circles";
    }

    @GetMapping("/{id}")
    public String getCircle(@PathVariable("id") String id, Model model) {
        model.addAttribute("circle", circleService.get(id).orElseThrow(RuntimeException::new)); //TODO exception
        model.addAttribute("newMemberCommand", new Person());
        return "circle";
    }

    @PostMapping("/{id}")
    public String updateMembers(@PathVariable("id") String id, @ModelAttribute Circle circleCommand) {
        circleService.get(id)
                .ifPresent(circle -> {
                    circle.setMembers(circleCommand.getMembers());
                    circleService.persist(circle);
                });
        return "redirect:/circles/" + id;

    }

    @PostMapping("/{id}/members/{member}")
    public String removeMember(@PathVariable("id") String circleId, @PathVariable("member") String memberEmail) {
        circleService.get(circleId)
                .ifPresent(circle -> {
                    circle.getMembers().remove(memberEmail);
                    circleService.persist(circle);
                });
        return "redirect:/circles/" + circleId;
    }

    @PostMapping("/{id}/members")
    public String addMember(@PathVariable("id") String circleId, @ModelAttribute Person newMember) {
        circleService.get(circleId)
                .ifPresent(circle -> {
                    if (circle.getMembers() == null) {
                        circle.setMembers(new LinkedList<>());
                    }
                    circle.getMembers().add(newMember.getEmail());
                    circleService.persist(circle);
                });
        return "redirect:/circles/" + circleId;
    }
}