package rniesler.gphotoshare.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import rniesler.gphotoshare.domain.Circle;
import rniesler.gphotoshare.domain.Person;
import rniesler.gphotoshare.exceptions.CircleNotFoundException;
import rniesler.gphotoshare.security.SecurityService;
import rniesler.gphotoshare.services.CircleService;

import javax.validation.Valid;
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
        model.addAttribute("newcircle", new Circle());
        return setListCirclesModel(model);
    }

    private String setListCirclesModel(Model model) {
        model.addAttribute("circles", circleService.findAll());
        return "circlelist";
    }

    @PostMapping({"", "/"})
    public String newCircle(@Valid @ModelAttribute("newcircle") Circle circle, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return setListCirclesModel(model);
        } else {
            circle.setOwner(securityService.getAuthenticatedEmail());
            circleService.persist(circle);
            return "redirect:/circles";
        }
    }

    @GetMapping("/{id}")
    public String getCircle(@PathVariable("id") String id, Model model) {
        model.addAttribute("newMemberCommand", new Person());
        return setCircleModel(id, model);
    }

    private String setCircleModel(String id, Model model) {
        model.addAttribute("circle", circleService.get(id).orElseThrow(CircleNotFoundException::new));
        return "circle";
    }

    @PostMapping("/{id}/delete")
    public String deleteCircle(@PathVariable("id") String id) {
        circleService.deleteCircle(id);
        return "redirect:/circles";
    }

    @PostMapping("/{id}/members/{member}")
    public String removeMember(@PathVariable("id") String circleId, @PathVariable("member") String memberEmail) {
        circleService.get(circleId)
                .ifPresentOrElse(circle -> {
                    circle.getMembers().remove(memberEmail);
                    circleService.persist(circle);
                }, () -> {
                    throw new CircleNotFoundException();
                });
        return "redirect:/circles/" + circleId;
    }

    @PostMapping("/{circleId}/members")
    public String addMember(@PathVariable("circleId") String circleId, @Valid @ModelAttribute("newMemberCommand") Person newMember,
                            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return setCircleModel(circleId, model);
        } else {
            circleService.get(circleId)
                    .ifPresentOrElse(circle -> {
                        if (circle.getMembers() == null) {
                            circle.setMembers(new LinkedList<>());
                        }
                        circle.getMembers().add(newMember.getEmail());
                        circleService.persist(circle);
                    }, () -> {
                        throw new CircleNotFoundException();
                    });
            return "redirect:/circles/" + circleId;
        }
    }
}
