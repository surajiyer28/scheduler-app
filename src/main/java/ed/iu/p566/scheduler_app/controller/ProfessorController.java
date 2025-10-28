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
import ed.iu.p566.scheduler_app.utilities.TimeSlotUtility;
import ed.iu.p566.scheduler_app.model.AppointmentGroup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.RequestParam;
import ed.iu.p566.scheduler_app.model.AvailabilitySlot;
import ed.iu.p566.scheduler_app.model.TimeSlot;


@Controller
@RequestMapping("/professor")
@SessionAttributes("currentUser")  
public class ProfessorController {

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

    // @GetMapping("/dashboard")
    // public String viewDashboard(
    //         @SessionAttribute(value = "currentUser", required = false) User user,
    //         Model model,
    //         RedirectAttributes redirectAttributes) {
        
        
    //     if (user == null || user.getId() == null) {
    //         redirectAttributes.addFlashAttribute("error", "Please login first");
    //         return "redirect:/";
    //     }

    //     List<AppointmentGroup> appointmentGroups = appointmentGroupRepository.getAppointmentGroupsByProfessorId(user.getId());
    //     model.addAttribute("currentUser", user);
    //     model.addAttribute("appointmentGroups", appointmentGroups);

    //     return "dashboard";
    // }

    @GetMapping("/appointments/create")
    public String viewCreateAppointments(
            @SessionAttribute(value = "currentUser", required = false) User user,
            // Model model,
            RedirectAttributes redirectAttributes) {
        
        
        if (user == null || user.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Please login first");
            return "redirect:/";
        }
        
        
        // model.addAttribute("currentUser", user);
        
        return "create-appointment";
    }

    @PostMapping("/appointments/create")
    public String createAppointmentGroup(
            @SessionAttribute(value = "currentUser", required = false) User user,
            @RequestParam String title,
            @RequestParam AppointmentType type,
            @RequestParam int durationPerSlot,
            @RequestParam(defaultValue = "0") int gapBetweenSlots,
            @RequestParam String[] dates,
            @RequestParam String[] startTimes,
            @RequestParam String[] endTimes,
            RedirectAttributes redirectAttributes) {
        
        if (user == null || user.getRole() != User.UserRole.PROFESSOR) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized request");
            return "redirect:/";
        }
        
        //creating availability windows from the input arrays
        List<AvailabilitySlot> slots = new ArrayList<>();
        for (int i = 0; i < dates.length; i++) {
            AvailabilitySlot slot = new AvailabilitySlot(

                LocalDate.parse(dates[i]),
                LocalTime.parse(startTimes[i]),
                LocalTime.parse(endTimes[i])
            );
            slots.add(slot);
        }
        
        // checking no availability windows overlap on the same date
        for (int i = 0; i < slots.size(); i++) {
            for (int j = i + 1; j < slots.size(); j++) {
                if (slots.get(i).checkOverlap(slots.get(j))) {
                    redirectAttributes.addFlashAttribute("error", "Availability windows cannot overlap on the same date");
                    return "redirect:/professor/appointments/create";
                }
            }
        }
        

        // earliest availability window date must be afer today
        LocalDate earliestDate = slots.stream()
                .map(AvailabilitySlot::getDate)
                .min(LocalDate::compareTo)
                .orElse(null);
        
        if (earliestDate != null && !earliestDate.isAfter(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "Earliest date must be in the future");
            return "redirect:/professor/appointments/create";
        }
        
        
        
        // creating appointment group object and saving to db
        AppointmentGroup appointmentGroup = new AppointmentGroup();
        appointmentGroup.setTitle(title);
        appointmentGroup.setType(type);
        appointmentGroup.setDurationPerSlot(durationPerSlot);
        appointmentGroup.setGapBetweenSlots(gapBetweenSlots);
        appointmentGroup.setAvailabilitySlots(slots);
        appointmentGroup.setProfessorId(user.getId());
        appointmentGroup.setCreatedAt(LocalDateTime.now());
        
        appointmentGroup = appointmentGroupRepository.save(appointmentGroup);

        List<TimeSlot> timeSlots = TimeSlotUtility.generateTimeSlots(appointmentGroup);
        timeSlotRepository.saveAll(timeSlots);
        
        // System.out.println("Created appointment group with " + slots.size() + " availability slots");
        
        redirectAttributes.addFlashAttribute("message", "Appointment group created successfully!");
        return "redirect:/dashboard";
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
        if (appointmentGroup == null || !appointmentGroup.getProfessorId().equals(user.getId())) {
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

        if (user.getRole().name() != "PROFESSOR") {
            redirectAttributes.addFlashAttribute("error", "Action not allowed");
            return "redirect:/ta/bookings/view/" + timeSlot.getAppointmentGroupId();
        }

        if (timeSlot == null) {
            redirectAttributes.addFlashAttribute("error", "Time slot not found");
            return "redirect:/professor/bookings/view/" + timeSlot.getAppointmentGroupId();
        }

        if (timeSlot.getStatus() != TimeSlot.BookingStatus.BOOKED && timeSlot.getStatus() != TimeSlot.BookingStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("error", "Time slot is already inactive");
            return "redirect:/professor/bookings/view/" + timeSlot.getAppointmentGroupId();
        }
        
        timeSlot.setStatus(TimeSlot.BookingStatus.CANCELLED);
        timeSlot.setBookedByUserId(null);
        timeSlot.setGroupMemberIdsList(new ArrayList<>());  
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

        if (user.getRole().name() != "PROFESSOR") {
            redirectAttributes.addFlashAttribute("error", "Action not allowed");
            return "redirect:/ta/bookings/view/";
        }

        // System.out.println("THE SLOT WAS CANCELLED***************************************************");
        TimeSlot timeSlot = timeSlotRepository.findById(slotId).orElse(null);

        if (user.getRole().name() != "PROFESSOR") {
            redirectAttributes.addFlashAttribute("error", "Action not allowed");
            return "redirect:/ta/bookings/view/" + timeSlot.getAppointmentGroupId();
        }

        if (timeSlot == null) {
            redirectAttributes.addFlashAttribute("error", "Time slot not found");
            return "redirect:/professor/bookings/view/" + timeSlot.getAppointmentGroupId();
        }

        if (timeSlot.getStatus() != TimeSlot.BookingStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("error", "Time slot is already active");
            return "redirect:/professor/bookings/view/" + timeSlot.getAppointmentGroupId();
        }
        
        timeSlot.setStatus(TimeSlot.BookingStatus.AVAILABLE);
        timeSlot.setBookedByUserId(null);
        timeSlotRepository.save(timeSlot);

        redirectAttributes.addFlashAttribute("message", "Time slot cancelled successfully");
        return "redirect:/professor/bookings/view/" + timeSlot.getAppointmentGroupId();
    }
    
}


