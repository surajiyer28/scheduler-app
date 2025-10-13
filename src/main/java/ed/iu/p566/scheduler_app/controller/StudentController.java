package ed.iu.p566.scheduler_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ed.iu.p566.scheduler_app.model.AppointmentGroup;
import ed.iu.p566.scheduler_app.model.User;
import ed.iu.p566.scheduler_app.repository.AppointmentGroupRepository;


@Controller
@RequestMapping("/student")
@SessionAttributes("currentUser")  
public class StudentController {

    @Autowired
    private AppointmentGroupRepository appointmentGroupRepository;
    

    @GetMapping("/dashboard")
    public String viewDashboard(
            @SessionAttribute(value = "currentUser", required = false) User user,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        
        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }
        
        Iterable<AppointmentGroup> appointmentGroups = appointmentGroupRepository.findAll();
        model.addAttribute("currentUser", user);
        model.addAttribute("appointmentGroups", appointmentGroups);

        return "dashboard";
    }

    
}



// @GetMapping("/professor")
//     public String viewProfessorDashboard(
//             @SessionAttribute(value = "currentUser", required = false) User user,
//             Model model,
//             RedirectAttributes redirectAttributes) {
        
//         if (user == null || user.getId() == null) {
//             redirectAttributes.addFlashAttribute("error", "Please login first");
//             return "redirect:/";
//         }
        
//         model.addAttribute("currentUser", user);
//         return "dashboard";
//     }

//     @GetMapping("/ta")
//     public String viewTADashboard(
//             @SessionAttribute(value = "currentUser", required = false) User user,
//             Model model,
//             RedirectAttributes redirectAttributes) {
        
//         if (user == null || user.getId() == null) {
//             redirectAttributes.addFlashAttribute("error", "Please login first");
//             return "redirect:/";
//         }
        
//         model.addAttribute("currentUser", user);
//         return "dashboard";
//     }