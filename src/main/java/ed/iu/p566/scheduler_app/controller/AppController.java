package ed.iu.p566.scheduler_app.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ed.iu.p566.scheduler_app.model.User;
import ed.iu.p566.scheduler_app.model.User.UserRole;
import ed.iu.p566.scheduler_app.repository.UserRepository;
import jakarta.validation.Valid;


@Controller
@RequestMapping("/")
@SessionAttributes("currentUser")
public class AppController {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute(name ="roles")
    public UserRole[] roles(){
        return UserRole.values();
    }

    @ModelAttribute(name = "user")
    public User user() {
        return new User();
    }

    @GetMapping("/signup")
    public String viewSignupPage() {
        return "signup";
    }

    @GetMapping("/")
    public String showLandingPage() {
        
        return "index";
    }

    @PostMapping("/signup")
    public String createUser(@Valid @ModelAttribute User user, RedirectAttributes redirectAttributes) {

        if (!user.getEmail().contains("@iu.edu")){
            redirectAttributes.addFlashAttribute("error", "Email must be an IU email.");
            return "redirect:/signup";
        }

        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isEmpty()) {
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Account created successfully!");
            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("error", "User already exists.");
        return "redirect:/signup";
    }

    @PostMapping("/login") 
    public String loginUser(@RequestParam String email, 
                        @RequestParam String password, 
                        RedirectAttributes redirectAttributes,
                        Model model) {

        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User foundUser = userOpt.get();
            if (foundUser.getPassword().equals(password)) {

                model.addAttribute("currentUser", foundUser);
                
                redirectAttributes.addFlashAttribute("message", "Login successful! Welcome, " + foundUser.getName());
                return "redirect:/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("error", "Incorrect password. Please try again.");
                return "redirect:/";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "We couldn't find an account with these credentials.");
            return "redirect:/";
        }
    }

    @GetMapping("/dashboard")
    public String viewDashboard(
            @SessionAttribute(value = "currentUser", required = false) User user,
            RedirectAttributes redirectAttributes) {

        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }
 
        if (user.getRole() == UserRole.STUDENT) {
            return "redirect:/student/dashboard";
        } else if (user.getRole() == UserRole.PROFESSOR) {
            return "redirect:/professor/dashboard";
        } else if (user.getRole() == UserRole.TA) {
            return "redirect:/ta/dashboard";
        }
        
        return "redirect:/";
    }
    

    @GetMapping("/logout")
    public String logout(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }
}
    