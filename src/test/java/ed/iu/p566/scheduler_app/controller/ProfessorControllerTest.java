package ed.iu.p566.scheduler_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

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
import ed.iu.p566.scheduler_app.model.User;
import ed.iu.p566.scheduler_app.model.User.UserRole;
import ed.iu.p566.scheduler_app.repository.AppointmentGroupRepository;
import ed.iu.p566.scheduler_app.repository.TimeSlotRepository;

@ExtendWith(MockitoExtension.class)
public class ProfessorControllerTest {

    @InjectMocks
    private ProfessorController professorController;
    
    @Mock 
    private AppointmentGroupRepository appointmentGroupRepository;

    @Mock 
    private TimeSlotRepository timeSlotRepository;

    private User professor;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void testSetup(){
        professor = new User("Test Prof","testprof@iu.edu","password",UserRole.PROFESSOR);
        professor.setId(1L);

        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    void testCreateAppointmentGroup_NullUser() {

        String[] dates = {"2025-11-01"};
        String[] startTimes = {"09:00"};
        String[] endTimes = {"11:00"};

    
        String result = professorController.createAppointmentGroup(null, "Test", AppointmentType.INDIVIDUAL, 30, 0,dates, startTimes, endTimes, redirectAttributes);

        assertEquals("redirect:/", result);
        verify(appointmentGroupRepository, never()).save(any(AppointmentGroup.class));
        
    }


    @Test
    void testCreateAppointmentGroup_Success(){

        String title = "Test Meetings";
        AppointmentType type = AppointmentType.INDIVIDUAL;
        int durationPerSlot = 15;
        int gapBetweenSlots = 0;
        String[] dates = { LocalDate.now().plusDays(1).toString() };
        String[] startTimes = {"09:00"};
        String[] endTimes = {"11:00"};

        AppointmentGroup testGroup = new AppointmentGroup();
        testGroup.setId(1L);
        when(appointmentGroupRepository.save(any(AppointmentGroup.class))).thenReturn(testGroup);

        String result = professorController.createAppointmentGroup(professor, title, type, durationPerSlot, gapBetweenSlots,dates, startTimes, endTimes, redirectAttributes);


        assertEquals("redirect:/dashboard", result);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("message"));

        ArgumentCaptor<AppointmentGroup> captor = ArgumentCaptor.forClass(AppointmentGroup.class);
        verify(appointmentGroupRepository).save(captor.capture());

        AppointmentGroup capturedValues = captor.getValue();
        assertEquals(title, capturedValues.getTitle());
        assertEquals(type, capturedValues.getType());
        assertEquals(durationPerSlot, capturedValues.getDurationPerSlot());
        assertEquals(gapBetweenSlots, capturedValues.getGapBetweenSlots());
        assertEquals(1L, capturedValues.getProfessorId());

    }

    @Test
    void testCreateAppointmentGroup_OverlappingWindowsFailure(){

        String title = "Test Meetings";
        AppointmentType type = AppointmentType.INDIVIDUAL;
        int durationPerSlot = 30;
        int gapBetweenSlots = 0;
        String[] dates = {"2025-12-01", "2025-12-01"};  
        String[] startTimes = {"09:00", "10:00"};        
        String[] endTimes = {"11:00", "12:00"};
 
        String result = professorController.createAppointmentGroup(professor, title, type, durationPerSlot, gapBetweenSlots,dates, startTimes, endTimes, redirectAttributes);

        assertEquals("redirect:/professor/appointments/create", result);
        assertEquals("Availability windows cannot overlap on the same date", redirectAttributes.getFlashAttributes().get("error"));
        
        verify(appointmentGroupRepository, never()).save(any(AppointmentGroup.class));

    }

    @Test
    void testCreateAppointmentGroup_InvalidDateFailure(){

        String title = "Test Meetings";
        AppointmentType type = AppointmentType.INDIVIDUAL;
        int durationPerSlot = 30;
        int gapBetweenSlots = 0;
        String[] dates = {LocalDate.now().toString()};
        String[] startTimes = {"09:00"};
        String[] endTimes = {"11:00"};
 
        String result = professorController.createAppointmentGroup(professor, title, type, durationPerSlot, gapBetweenSlots,dates, startTimes, endTimes, redirectAttributes);

        assertEquals("redirect:/professor/appointments/create", result);
        assertEquals("Earliest date must be in the future", redirectAttributes.getFlashAttributes().get("error"));
        
        verify(appointmentGroupRepository, never()).save(any(AppointmentGroup.class));

    }

    @Test
    void testCreateAppointmentGroup_InvalidRoleFailure(){

        String title = "Test Meetings";
        AppointmentType type = AppointmentType.INDIVIDUAL;
        int durationPerSlot = 30;
        int gapBetweenSlots = 0;
        String[] dates = {LocalDate.now().toString()};
        String[] startTimes = {"09:00"};
        String[] endTimes = {"11:00"};
 
        String result = professorController.createAppointmentGroup(new User("Test Prof","testprof@iu.edu","password",UserRole.STUDENT), title, type, durationPerSlot, gapBetweenSlots,dates, startTimes, endTimes, redirectAttributes);

        assertEquals("redirect:/", result);
        assertEquals("Unauthorized request", redirectAttributes.getFlashAttributes().get("error"));
        
        verify(appointmentGroupRepository, never()).save(any(AppointmentGroup.class));

    }

    

}
