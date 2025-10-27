package ed.iu.p566.scheduler_app.controller;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ed.iu.p566.scheduler_app.model.User;
import ed.iu.p566.scheduler_app.model.AppointmentGroup.AppointmentType;
import ed.iu.p566.scheduler_app.repository.AppointmentGroupRepository;
import ed.iu.p566.scheduler_app.repository.TimeSlotRepository;
import ed.iu.p566.scheduler_app.repository.UserRepository;
import ed.iu.p566.scheduler_app.model.AppointmentGroup;

import java.util.ArrayList;
import org.springframework.web.bind.annotation.RequestParam;
import ed.iu.p566.scheduler_app.model.TimeSlot;


@Controller
@RequestMapping("/ta")
@SessionAttributes("currentUser")  
public class TAController {

    @Autowired
    private AppointmentGroupRepository appointmentGroupRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute(name = "types")
    public AppointmentType[] types() {
        return AppointmentType.values();
    }

    @ModelAttribute("appointmentGroups")
    public Iterable<AppointmentGroup> getAppointmentGroups(
            @SessionAttribute(value = "currentUser", required = false) User user) {
        if (user == null || user.getId() == null) {
            return new ArrayList<>(); 
        }
        return appointmentGroupRepository.findAll();
    }

    @GetMapping("/dashboard")
    public String viewDashboard(
            @SessionAttribute(value = "currentUser", required = false) User user,
            RedirectAttributes redirectAttributes) {
        
        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }
        
        return "dashboard";
    }

    

    @GetMapping("/bookings/view/{id}")
    public String viewBooking(
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
            redirectAttributes.addFlashAttribute("error", "Appointment group not found or access denied");
            return "redirect:/dashboard";
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findByAppointmentGroupIdOrderByDateAscStartTimeAsc(id);

        for (TimeSlot slot : timeSlots) {
            if (slot.getStatus() == TimeSlot.BookingStatus.BOOKED && slot.getBookedByUserId() != null) {
                Optional<User> bookedByUser = userRepository.findById(slot.getBookedByUserId());
                if (bookedByUser.isPresent()) {
                    slot.setBookedByUserName(bookedByUser.get().getName());
                    slot.setBookedByUserEmail(bookedByUser.get().getEmail());
                }
                if (appointmentGroup.getType() == AppointmentGroup.AppointmentType.GROUP) {
                    List<Long> memberIds = slot.getGroupMemberIdsList();
                    if (!memberIds.isEmpty()) {
                        List<String> memberNames = new ArrayList<>();
                        for (Long memberId : memberIds) {
                            Optional<User> member = userRepository.findById(memberId);
                            if (member.isPresent()) {
                                memberNames.add(member.get().getName());
                            }
                        }

                        if (!memberNames.isEmpty()) {
                            slot.setBookedByUserEmail(slot.getBookedByUserEmail() + "||" + String.join(", ", memberNames));
                        }
                    }
                }
            }
        }

        // model.addAttribute("currentUser", user);
        model.addAttribute("appointmentGroup", appointmentGroup);
        model.addAttribute("timeSlots", timeSlots);

        return "booking";
    }

    // this method is used to make a timeslot unavailable for booking
    @PostMapping("/bookings/cancel")
    public String cancelBooking(
            @SessionAttribute(value = "currentUser", required = false) User user,
            @RequestParam Long slotId,
            RedirectAttributes redirectAttributes) {
        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }
        // System.out.println("THE SLOT WAS CANCELLED***************************************************");
        TimeSlot timeSlot = timeSlotRepository.findById(slotId).orElse(null);
        if (timeSlot == null) {
            redirectAttributes.addFlashAttribute("error", "Time slot not found");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
        }

        if (timeSlot.getStatus() != TimeSlot.BookingStatus.BOOKED && timeSlot.getStatus() != TimeSlot.BookingStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("error", "Time slot is already inactive");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
        }
        
        timeSlot.setStatus(TimeSlot.BookingStatus.CANCELLED);
        timeSlot.setBookedByUserId(null);
        timeSlotRepository.save(timeSlot);

        redirectAttributes.addFlashAttribute("message", "Time slot cancelled successfully");
        return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
    }

    // this method is used to make a timeslot available for booking again
    @PostMapping("/bookings/avail")
    public String availBooking(
            @SessionAttribute(value = "currentUser", required = false) User user,
            @RequestParam Long slotId,
            RedirectAttributes redirectAttributes) {
        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }
        // System.out.println("THE SLOT WAS CANCELLED***************************************************");
        TimeSlot timeSlot = timeSlotRepository.findById(slotId).orElse(null);
        if (timeSlot == null) {
            redirectAttributes.addFlashAttribute("error", "Time slot not found");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
        }

        if (timeSlot.getStatus() != TimeSlot.BookingStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("error", "Time slot is already active");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
        }
        
        timeSlot.setStatus(TimeSlot.BookingStatus.AVAILABLE);
        timeSlot.setBookedByUserId(null);
        timeSlotRepository.save(timeSlot);

        redirectAttributes.addFlashAttribute("message", "Time slot cancelled successfully");
        return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
    }
    
}


