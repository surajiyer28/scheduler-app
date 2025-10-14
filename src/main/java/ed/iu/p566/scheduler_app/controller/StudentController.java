package ed.iu.p566.scheduler_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ed.iu.p566.scheduler_app.model.AppointmentGroup;
import ed.iu.p566.scheduler_app.model.TimeSlot;
import ed.iu.p566.scheduler_app.model.User;
import ed.iu.p566.scheduler_app.repository.AppointmentGroupRepository;
import ed.iu.p566.scheduler_app.repository.TimeSlotRepository;


@Controller
@RequestMapping("/student")
@SessionAttributes("currentUser")  
public class StudentController {

    @Autowired
    private AppointmentGroupRepository appointmentGroupRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;
    

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

    @GetMapping("/bookings/book/{id}")
    public String viewBookingForStudent(
            @SessionAttribute(value = "currentUser", required = false) User user,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }

        AppointmentGroup appointmentGroup = appointmentGroupRepository.findById(id).orElse(null);
        if (appointmentGroup == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment group not found");
            return "redirect:/dashboard";
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findByAppointmentGroupIdOrderByDateAscStartTimeAsc(id);

        model.addAttribute("currentUser", user);
        model.addAttribute("appointmentGroup", appointmentGroup);
        model.addAttribute("timeSlots", timeSlots);

        return "booking";
    }
    

    // need updates for group appointment booking
    @PostMapping("/bookings/book")
    public String bookSlot(
            @SessionAttribute(value = "currentUser", required = false) User user,
            @RequestParam Long slotId,
            RedirectAttributes redirectAttributes) {
        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }

        TimeSlot timeSlot = timeSlotRepository.findById(slotId).orElse(null);
        if (timeSlot == null) {
            redirectAttributes.addFlashAttribute("error", "Time slot not found");
            return "redirect:/student/dashboard";
        }
        
        // one booking per user - need to update this for group appointment booking
        boolean userBooked = timeSlotRepository.existsByAppointmentGroupIdAndBookedByUserId(timeSlot.getAppointmentGroupId(), user.getId());

        if (userBooked) {
            redirectAttributes.addFlashAttribute("error", "You have already booked a time slot in this appointment group");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();        
        }

        if (timeSlot.getStatus() != TimeSlot.BookingStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("error", "Time slot already booked");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();        
        }

        timeSlot.setStatus(TimeSlot.BookingStatus.BOOKED);
        timeSlot.setBookedByUserId(user.getId());
        timeSlotRepository.save(timeSlot);

        redirectAttributes.addFlashAttribute("message", "Time slot booked successfully");
        return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
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