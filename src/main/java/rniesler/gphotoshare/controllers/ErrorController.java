package rniesler.gphotoshare.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/errors")
public class ErrorController {
    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "errors/accessDenied";
    }
}
