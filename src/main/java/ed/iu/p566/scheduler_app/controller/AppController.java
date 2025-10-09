package ed.iu.p566.scheduler_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {
    
    @GetMapping("/")
    public String viewLandingPage() {
        return "index";
    }
}
