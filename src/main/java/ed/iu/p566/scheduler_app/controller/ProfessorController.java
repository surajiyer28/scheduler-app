package ed.iu.p566.scheduler_app.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ed.iu.p566.scheduler_app.model.User;
import jakarta.validation.Valid;
import ed.iu.p566.scheduler_app.model.AppointmentGroup.AppointmentType;
import ed.iu.p566.scheduler_app.repository.AppointmentGroupRepository;
import ed.iu.p566.scheduler_app.model.AppointmentGroup;


@Controller
@RequestMapping("/professor")
@SessionAttributes("currentUser")  
public class ProfessorController {

    @Autowired
    private AppointmentGroupRepository appointmentGroupRepository;

    @ModelAttribute(name = "types")
    public AppointmentType[] types() {
        return AppointmentType.values();
    }

    @ModelAttribute(name = "appointmentGroup")
    public AppointmentGroup appointmentGroup() {
        return new AppointmentGroup();
    }

    @GetMapping("/dashboard")
    public String viewDashboard(
            @SessionAttribute(value = "currentUser", required = false) User user,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        
        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }

        List<AppointmentGroup> appointmentGroups = appointmentGroupRepository.getAppointmentGroupsByProfessorId(user.getId());
        model.addAttribute("currentUser", user);
        model.addAttribute("appointmentGroups", appointmentGroups);

        return "dashboard";
    }

    @GetMapping("/appointments/create")
    public String viewCreateAppointments(
            @SessionAttribute(value = "currentUser", required = false) User user,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        
        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }
        
        
        model.addAttribute("currentUser", user);
        
        return "create-appointment";
    }

    @PostMapping("/appointments/create")
    public String createAppointmentGroup(
            @SessionAttribute(value = "currentUser", required = false) User user,
            @Valid @ModelAttribute("appointmentGroup") AppointmentGroup appointmentGroup,
            RedirectAttributes redirectAttributes) {
        
        
        if (user == null || user.getRole() != User.UserRole.PROFESSOR) {
            return "redirect:/";
        }
        
        // Validating that availabilities can only be created for future dates (until a day before the first availability date)
        String[] datesArray = appointmentGroup.getDates().split(",");
        LocalDate earliestDate = Arrays.stream(datesArray)
                .map(LocalDate::parse)
                .min(LocalDate::compareTo)
                .orElse(null);
        
        if (earliestDate != null && !earliestDate.isAfter(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "Earliest date must be in the future");
            return "redirect:/professor/appointments/create";
        }

        appointmentGroup.setProfessorId(user.getId());
        appointmentGroup.setCreatedAt(LocalDateTime.now());
        System.out.println("Professor ID: " + appointmentGroup.getProfessorId() + ", Title: " + appointmentGroup.getTitle()  + ", Type: " + appointmentGroup.getType() + ", DurationPerSlot: " + appointmentGroup.getDurationPerSlot() + ", Dates: " + appointmentGroup.getDates() + ", StartTimes: " + appointmentGroup.getStartTimes() + ", EndTimes: " + appointmentGroup.getEndTimes());

        
        appointmentGroupRepository.save(appointmentGroup);

        redirectAttributes.addFlashAttribute("success", "Appointment group created successfully!");
        return "redirect:/dashboard";
        
        
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