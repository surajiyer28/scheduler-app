package ed.iu.p566.scheduler_app.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import ed.iu.p566.scheduler_app.repository.UserRepository;


@Controller
@RequestMapping("/student")
@SessionAttributes("currentUser")  
public class StudentController {

    @Autowired
    private AppointmentGroupRepository appointmentGroupRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private UserRepository userRepository;
    

    @ModelAttribute("appointmentGroups")
    public Iterable<AppointmentGroup> getAppointmentGroups(
            @SessionAttribute(value = "currentUser", required = false) User user) {
        if (user == null || user.getId() == null) {
            return new ArrayList<>(); 
        }
        return appointmentGroupRepository.findAll();
    }


    @ModelAttribute("upcomingAppointments")
    public List<Map<String,Object>> getUpcomingAppointments(@SessionAttribute(value = "currentUser", required = false) User user) {


        if (user == null || user.getId() == null) {
            return new ArrayList<>();
        }

        LocalDate curr_date = LocalDate.now();
        LocalTime curr_time = LocalTime.now();
        
        // List<TimeSlot> upcomingAppointments = timeSlotRepository.findByBookedByUserIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(user.getId(), LocalDate.now());
        List<TimeSlot> allBookedSlots = timeSlotRepository.findByStatusAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(TimeSlot.BookingStatus.BOOKED, curr_date);
        List<Map<String,Object>> slotsWithTitle = new ArrayList<>();

        for (TimeSlot slot :allBookedSlots) {
            if (slot.getDate().isEqual(curr_date) && slot.getEndTime().isBefore(curr_time)) {
                continue;
            }

            boolean isBooker = slot.getBookedByUserId() != null && slot.getBookedByUserId().equals(user.getId());
            boolean isMember = slot.getGroupMemberIdsList().contains(user.getId());

            if (isBooker || isMember) {
                AppointmentGroup group = appointmentGroupRepository.findById(slot.getAppointmentGroupId()).orElse(null);
                
                Map<String,Object> slotInfo = Map.of(
                    "id", slot.getId(),
                    "date", slot.getDate(),
                    "startTime", slot.getStartTime(),
                    "endTime", slot.getEndTime(),
                    "appointmentGroup", group != null ? group : new AppointmentGroup()
                );
                slotsWithTitle.add(slotInfo);  
            }
        }

        // System.out.println("Upcoming appointments for user " + user.getId() + ": " + upcomingAppointments);

        return slotsWithTitle;
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

        // for group bookings
        if (appointmentGroup.getType() == AppointmentGroup.AppointmentType.GROUP) {
            List<User> otherStudents = userRepository.findAllStudentsExcept(user.getId());
            model.addAttribute("otherStudents", otherStudents);
        }

        // model.addAttribute("currentUser", user);
        model.addAttribute("appointmentGroup", appointmentGroup);
        model.addAttribute("timeSlots", timeSlots);

        return "booking";
    }
    

    // need updates for group appointment booking
    @PostMapping("/bookings/book")
    public String bookSlot(
            @SessionAttribute(value = "currentUser", required = false) User user,
            @RequestParam Long slotId,
            @RequestParam(required = false) String groupMembers,      //group bookings      
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

        LocalDate slotDate = timeSlot.getDate();
        LocalTime slotStartTime = timeSlot.getStartTime();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        boolean isPast = slotDate.isBefore(today) || 
                        (slotDate.isEqual(today) && slotStartTime.isBefore(now));

        if (isPast) {
            redirectAttributes.addFlashAttribute("error", "Cannot book/cancel past time slots");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
        }

        AppointmentGroup appointmentGroup = appointmentGroupRepository.findById(timeSlot.getAppointmentGroupId()).orElse(null);
        if (appointmentGroup == null) {
            redirectAttributes.addFlashAttribute("error", "Appointment group not found");
            return "redirect:/student/dashboard";
        }

        if (timeSlot.getStatus() != TimeSlot.BookingStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("error", "Time slot already booked");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();        
        }
        
        // one booking per user - need to update this for group appointment booking
        // boolean userBooked = timeSlotRepository.existsByAppointmentGroupIdAndBookedByUserId(timeSlot.getAppointmentGroupId(), user.getId());
        boolean userBooked = timeSlotRepository.isUserInAnyBookingInGroup(timeSlot.getAppointmentGroupId(), user.getId());

        if (userBooked) {
            redirectAttributes.addFlashAttribute("error", "You have already booked a time slot in this appointment group. Cancel your existing booking to book a new one.");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();        
        }

        List<Long> memberIds = new ArrayList<>();
        if (groupMembers != null && !groupMembers.isEmpty()) {
            memberIds = Arrays.stream(groupMembers.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        // checking if selected members have other active bookings for the same appointment group
        if (appointmentGroup.getType() == AppointmentGroup.AppointmentType.GROUP){
            if (!memberIds.isEmpty()) {
            
                for (Long memberId : memberIds) {
                    boolean memberBooked = timeSlotRepository.isUserInAnyBookingInGroup(timeSlot.getAppointmentGroupId(), memberId);
                    
                    if (memberBooked) {

                        User member = userRepository.findById(memberId).orElse(null);
                        String memberName = member.getName();

                        redirectAttributes.addFlashAttribute("error", memberName + " already has another booking for this appointment group.");
                        return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
                    }
                }

                timeSlot.setGroupMemberIdsList(memberIds);
            } else{
                redirectAttributes.addFlashAttribute("error", "This booking requires at least one other group member");
                return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
            }
        }

        timeSlot.setStatus(TimeSlot.BookingStatus.BOOKED);
        timeSlot.setBookedByUserId(user.getId());
        timeSlotRepository.save(timeSlot);

        redirectAttributes.addFlashAttribute("message", "Time slot booked successfully");
        return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
    }


    // this method enables a student to cancel their booking and makes the timeslot available again
    @PostMapping("/bookings/cancel")
    public String cancelBooking(
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
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
        }

        LocalDate slotDate = timeSlot.getDate();
        LocalTime slotStartTime = timeSlot.getStartTime();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        boolean isPast = slotDate.isBefore(today) || 
                        (slotDate.isEqual(today) && slotStartTime.isBefore(now));

        if (isPast) {
            redirectAttributes.addFlashAttribute("error", "Cannot book/cancel past time slots");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
        }

        if (timeSlot.getStatus() != TimeSlot.BookingStatus.BOOKED) {
            redirectAttributes.addFlashAttribute("error", "Time slot not booked");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
        }
        
        List<Long> groupMembers = timeSlot.getGroupMemberIdsList();

        if (!user.getId().equals(timeSlot.getBookedByUserId()) && !groupMembers.contains(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Not Allowed");
            return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
        }

        timeSlot.setStatus(TimeSlot.BookingStatus.AVAILABLE);
        timeSlot.setBookedByUserId(null);
        timeSlot.setGroupMemberIdsList(new ArrayList<>());  
        timeSlotRepository.save(timeSlot);

        redirectAttributes.addFlashAttribute("message", "Time slot cancelled successfully");
        return "redirect:/student/bookings/book/" + timeSlot.getAppointmentGroupId();
    }

}

