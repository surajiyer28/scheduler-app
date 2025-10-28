package ed.iu.p566.scheduler_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import ed.iu.p566.scheduler_app.model.AppointmentGroup;
import ed.iu.p566.scheduler_app.model.AppointmentGroup.AppointmentType;
import ed.iu.p566.scheduler_app.model.TimeSlot;
import ed.iu.p566.scheduler_app.model.TimeSlot.BookingStatus;
import ed.iu.p566.scheduler_app.model.User;
import ed.iu.p566.scheduler_app.model.User.UserRole;
import ed.iu.p566.scheduler_app.repository.AppointmentGroupRepository;
import ed.iu.p566.scheduler_app.repository.TimeSlotRepository;
import ed.iu.p566.scheduler_app.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class StudentControllerTest {
    
    @InjectMocks
    private StudentController studentController;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private AppointmentGroupRepository appointmentGroupRepository;

    @Mock
    private UserRepository userRepository;

    private User student1;
    private User student2;

    private AppointmentGroup appointmentGroup1;
    private AppointmentGroup appointmentGroup2;
    private TimeSlot timeSlot1;
    private TimeSlot timeSlot2;

    private RedirectAttributes redirectAttributes;


    @BeforeEach
    void testSetup() {

        student1 = new User("TestStudent1", "teststudent1@iu.edu", "password", UserRole.STUDENT);
        student1.setId(1L);

        student2 = new User("TestStudent2", "teststudent2@iu.edu", "password", UserRole.STUDENT);
        student2.setId(2L);

            // appointmentGroup2 = Individual appointment type
        appointmentGroup1 = new AppointmentGroup();
        appointmentGroup1.setId(10L);
        appointmentGroup1.setTitle("Test Individual");
        appointmentGroup1.setType(AppointmentType.INDIVIDUAL);
        appointmentGroup1.setProfessorId(99L);


        // appointmentGroup2 = Group appointment type
        appointmentGroup2 = new AppointmentGroup();
        appointmentGroup2.setId(20L);
        appointmentGroup2.setTitle("Test Group");
        appointmentGroup2.setType(AppointmentType.GROUP);
        appointmentGroup2.setProfessorId(99L);

        
        timeSlot1 = new TimeSlot();
        timeSlot1.setId(100L);
        timeSlot1.setAppointmentGroupId(10L);
        timeSlot1.setDate(LocalDate.now().plusDays(1));
        timeSlot1.setStartTime(LocalTime.of(9, 0));
        timeSlot1.setEndTime(LocalTime.of(9, 15));
        timeSlot1.setStatus(BookingStatus.AVAILABLE);

        timeSlot2 = new TimeSlot();
        timeSlot2.setId(200L);
        timeSlot2.setAppointmentGroupId(20L);
        timeSlot2.setDate(LocalDate.now().plusDays(1));
        timeSlot2.setStartTime(LocalTime.of(9, 0));
        timeSlot2.setEndTime(LocalTime.of(9, 30));
        timeSlot2.setStatus(BookingStatus.AVAILABLE);

        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    void testBookSlot_NullUser() {

        String result = studentController.bookSlot(null, 100L, null,redirectAttributes);

        assertEquals("redirect:/", result);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("error"));
        
        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void testBookSlot_IndividualSuccess() {
   
        when(timeSlotRepository.findById(100L)).thenReturn(Optional.of(timeSlot1));
        when(appointmentGroupRepository.findById(10L)).thenReturn(Optional.of(appointmentGroup1));
        when(timeSlotRepository.isUserInAnyBookingInGroup(10L, 1L)).thenReturn(false);

        String result = studentController.bookSlot(student1,100L,null, redirectAttributes);

        assertEquals("redirect:/student/bookings/book/10", result);
        assertEquals("Time slot booked successfully", redirectAttributes.getFlashAttributes().get("message"));

        ArgumentCaptor<TimeSlot> captor = ArgumentCaptor.forClass(TimeSlot.class);
        
        verify(timeSlotRepository).save(captor.capture());

        TimeSlot savedSlot = captor.getValue();

        assertEquals(BookingStatus.BOOKED, savedSlot.getStatus());
        assertEquals(1L, savedSlot.getBookedByUserId());

    }

    @Test
    void testBookSlot_BookedFailure() {
   
        when(timeSlotRepository.findById(100L)).thenReturn(Optional.of(timeSlot1));
        when(appointmentGroupRepository.findById(10L)).thenReturn(Optional.of(appointmentGroup1));
        when(timeSlotRepository.isUserInAnyBookingInGroup(10L, 1L)).thenReturn(true);

        String result = studentController.bookSlot(student1,100L,null, redirectAttributes);

        assertEquals("redirect:/student/bookings/book/10", result);
        assertEquals("You have already booked a time slot in this appointment group. Cancel your existing booking to book a new one.", redirectAttributes.getFlashAttributes().get("error"));

        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void testBookSlot_UnavailableFailure() {

        timeSlot1.setStatus(BookingStatus.BOOKED);
   
        when(timeSlotRepository.findById(100L)).thenReturn(Optional.of(timeSlot1));
        when(appointmentGroupRepository.findById(10L)).thenReturn(Optional.of(appointmentGroup1));

        String result = studentController.bookSlot(student1,100L,null, redirectAttributes);

        assertEquals("redirect:/student/bookings/book/10", result);
        assertEquals("Time slot already booked",redirectAttributes.getFlashAttributes().get("error"));
        verify(timeSlotRepository, never()).save(any());
    }

    @Test
    void testBookSlot_GroupSuccess() {
   
        when(timeSlotRepository.findById(200L)).thenReturn(Optional.of(timeSlot2));
        when(appointmentGroupRepository.findById(20L)).thenReturn(Optional.of(appointmentGroup2));
        when(timeSlotRepository.isUserInAnyBookingInGroup(20L, 1L)).thenReturn(false);  
        when(timeSlotRepository.isUserInAnyBookingInGroup(20L, 2L)).thenReturn(false);

        

        String result = studentController.bookSlot(student1,200L,"2", redirectAttributes);

        assertEquals("redirect:/student/bookings/book/20", result);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("message"));

        ArgumentCaptor<TimeSlot> captor = ArgumentCaptor.forClass(TimeSlot.class);
        verify(timeSlotRepository).save(captor.capture());

        TimeSlot savedSlot = captor.getValue();
        assertEquals(BookingStatus.BOOKED, savedSlot.getStatus());

        assertEquals(1L, savedSlot.getBookedByUserId());
        assertTrue(savedSlot.getGroupMemberIdsList().contains(2L), "student2 should be in group members list");

    }

    @Test
    void testBookSlot_NoMemberFailure() {
   
        when(timeSlotRepository.findById(200L)).thenReturn(Optional.of(timeSlot2));
        when(appointmentGroupRepository.findById(20L)).thenReturn(Optional.of(appointmentGroup2));
        when(timeSlotRepository.isUserInAnyBookingInGroup(20L, 1L)).thenReturn(false);

        

        String result = studentController.bookSlot(student1,200L,null, redirectAttributes);

        assertEquals("redirect:/student/bookings/book/20", result);
        assertEquals("This booking requires at least one other group member", redirectAttributes.getFlashAttributes().get("error"));

        verify(timeSlotRepository, never()).save(any());

    }

    @Test
    void testBookSlot_BookedMemberFailure() {
   
        when(timeSlotRepository.findById(200L)).thenReturn(Optional.of(timeSlot2));
        when(appointmentGroupRepository.findById(20L)).thenReturn(Optional.of(appointmentGroup2));
        when(timeSlotRepository.isUserInAnyBookingInGroup(20L, 1L)).thenReturn(false); 
        when(timeSlotRepository.isUserInAnyBookingInGroup(20L, 2L)).thenReturn(true); 
        when(userRepository.findById(2L)).thenReturn(Optional.of(student2));

        

        String result = studentController.bookSlot(student1,200L,"2", redirectAttributes);

        assertEquals("redirect:/student/bookings/book/20", result);
        assertEquals("TestStudent2 already has another booking for this appointment group.", redirectAttributes.getFlashAttributes().get("error"));
        
        verify(timeSlotRepository, never()).save(any());

    }


}
