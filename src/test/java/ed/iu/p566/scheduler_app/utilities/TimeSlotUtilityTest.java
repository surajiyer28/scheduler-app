package ed.iu.p566.scheduler_app.utilities;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ed.iu.p566.scheduler_app.model.AppointmentGroup;
import ed.iu.p566.scheduler_app.model.AvailabilitySlot;
import ed.iu.p566.scheduler_app.model.TimeSlot;
import ed.iu.p566.scheduler_app.model.AppointmentGroup.AppointmentType;
import ed.iu.p566.scheduler_app.model.TimeSlot.BookingStatus;

public class TimeSlotUtilityTest {

    @Test
    void testGenerateTimeSlots_NullAppointmentGroup() {

        List<TimeSlot> result = TimeSlotUtility.generateTimeSlots(null);

        assertNotNull(result, "Should return non-null list");
        assertTrue(result.isEmpty(), "Should return empty list for null input");
    }
    

    @Test
    void testGenerateTimeSlots_EmptyAvailabilitySlots() {

        AppointmentGroup group = new AppointmentGroup();
        group.setId(8L);
        group.setDurationPerSlot(30);
        group.setGapBetweenSlots(0);
        group.setAvailabilitySlots(new ArrayList<>());
        
        List<TimeSlot> result = TimeSlotUtility.generateTimeSlots(group);
        
        assertTrue(result.isEmpty(), "Should return empty list when no availability slots");
    }


    // test to check if the right count of timeslots is generated based on the availability window provided and whether the start and end times of time slots are as expected 
    @Test
    public void testGenerateTimeSlots_SingleWindowNoGap(){

        AppointmentGroup appointmentGroup = new AppointmentGroup();
        appointmentGroup.setId(1L);
        appointmentGroup.setTitle("Test Appointment Goup");
        appointmentGroup.setType(AppointmentType.GROUP);
        appointmentGroup.setDurationPerSlot(25);
        appointmentGroup.setGapBetweenSlots(0);
        
        LocalDate testDate = LocalDate.now().plusDays(1);
        LocalTime testStartTime = LocalTime.of(12,0);
        LocalTime testEndTime = LocalTime.of(16,0);

        List<AvailabilitySlot> slots = new ArrayList<>();
        slots.add(new AvailabilitySlot(testDate,testStartTime,testEndTime));
        appointmentGroup.setAvailabilitySlots(slots);

        

        List<TimeSlot> generatedTimeSlots = TimeSlotUtility.generateTimeSlots(appointmentGroup);

        // verify count
        assertEquals(9,generatedTimeSlots.size(),"There should be 9 time slots generated");

        TimeSlot firstSlot = generatedTimeSlots.get(0);
        TimeSlot lastSlot = generatedTimeSlots.get(generatedTimeSlots.size()-1);
        
        assertEquals(1L,firstSlot.getAppointmentGroupId());
        assertEquals(testDate,firstSlot.getDate());

        // first slot start and end time
        assertEquals(testStartTime,firstSlot.getStartTime());
        assertEquals(testStartTime.plusMinutes(25),firstSlot.getEndTime());

        // last slot start and end time
        assertEquals(LocalTime.of(15,20), lastSlot.getStartTime());
        assertEquals(LocalTime.of(15,45), lastSlot.getEndTime());

        assertEquals(BookingStatus.AVAILABLE,firstSlot.getStatus());

    }


    // test to check if the right count of timeslots is generated based on the availability window provided and whether the start and end times of time slots are as expected when there is a gap provided
    @Test
    public void testGenerateTimeSlots_SingleWindowWithGap(){

        AppointmentGroup appointmentGroup = new AppointmentGroup();
        appointmentGroup.setId(1L);
        appointmentGroup.setTitle("Test Appointment Goup");
        appointmentGroup.setType(AppointmentType.GROUP);
        appointmentGroup.setDurationPerSlot(20);
        appointmentGroup.setGapBetweenSlots(10);
        
        LocalDate testDate = LocalDate.now().plusDays(1);
        LocalTime testStartTime = LocalTime.of(12,0);
        LocalTime testEndTime = LocalTime.of(16,0);

        List<AvailabilitySlot> slots = new ArrayList<>();
        slots.add(new AvailabilitySlot(testDate,testStartTime,testEndTime));
        appointmentGroup.setAvailabilitySlots(slots);

        List<TimeSlot> generatedTimeSlots = TimeSlotUtility.generateTimeSlots(appointmentGroup);

        // verify count
        assertEquals(8,generatedTimeSlots.size(),"There should be 8 time slots generated");

        TimeSlot firstSlot = generatedTimeSlots.get(0);
        TimeSlot lastSlot = generatedTimeSlots.get(generatedTimeSlots.size()-1);
        
        assertEquals(1L,firstSlot.getAppointmentGroupId());
        assertEquals(testDate,firstSlot.getDate());

        // first slot start and end time
        assertEquals(testStartTime,firstSlot.getStartTime());
        assertEquals(testStartTime.plusMinutes(20),firstSlot.getEndTime());

        // last slot start and end time
        assertEquals(LocalTime.of(15,30), lastSlot.getStartTime());
        assertEquals(LocalTime.of(15,50), lastSlot.getEndTime());
        
        assertEquals(BookingStatus.AVAILABLE,firstSlot.getStatus());

    }

    @Test
    public void testGenerateTimeSlots_MultipleWindowNoGap(){

        AppointmentGroup appointmentGroup = new AppointmentGroup();
        appointmentGroup.setId(1L);
        appointmentGroup.setTitle("Test Appointment Goup");
        appointmentGroup.setType(AppointmentType.GROUP);
        appointmentGroup.setDurationPerSlot(25);
        appointmentGroup.setGapBetweenSlots(0);
        

        List<AvailabilitySlot> slots = new ArrayList<>();
        slots.add(new AvailabilitySlot(LocalDate.now().plusDays(1),LocalTime.of(12,0),LocalTime.of(15,0)));
        slots.add(new AvailabilitySlot(LocalDate.now().plusDays(1),LocalTime.of(16,0),LocalTime.of(18,0)));
        appointmentGroup.setAvailabilitySlots(slots);

        List<TimeSlot> generatedTimeSlots = TimeSlotUtility.generateTimeSlots(appointmentGroup);

        // verify count
        assertEquals(11,generatedTimeSlots.size(),"There should be 11 time slots generated");

        TimeSlot firstSlot = generatedTimeSlots.get(0);
        TimeSlot midSlot1 = generatedTimeSlots.get(6);
        TimeSlot midSlot2 = generatedTimeSlots.get(7);
        TimeSlot lastSlot = generatedTimeSlots.get(generatedTimeSlots.size()-1);
        
        assertEquals(1L,firstSlot.getAppointmentGroupId());
        assertEquals(LocalDate.now().plusDays(1),firstSlot.getDate());

        // first slot start and end time
        assertEquals(LocalTime.of(12,0),firstSlot.getStartTime());
        assertEquals(LocalTime.of(12,0).plusMinutes(25),firstSlot.getEndTime());

        // middle slot start and end time
        assertEquals(LocalTime.of(14,30), midSlot1.getStartTime());
        assertEquals(LocalTime.of(14,55), midSlot1.getEndTime());

        assertEquals(LocalTime.of(16,00), midSlot2.getStartTime());
        assertEquals(LocalTime.of(16,25), midSlot2.getEndTime());

        // last slot start and end time
        assertEquals(LocalTime.of(17,15), lastSlot.getStartTime());
        assertEquals(LocalTime.of(17,40), lastSlot.getEndTime());
        
        assertEquals(BookingStatus.AVAILABLE,firstSlot.getStatus());

    }

    @Test
    public void testGenerateTimeSlots_MultipleWindowWithGap(){

        AppointmentGroup appointmentGroup = new AppointmentGroup();
        appointmentGroup.setId(1L);
        appointmentGroup.setTitle("Test Appointment Goup");
        appointmentGroup.setType(AppointmentType.GROUP);
        appointmentGroup.setDurationPerSlot(20);
        appointmentGroup.setGapBetweenSlots(10);
        

        List<AvailabilitySlot> slots = new ArrayList<>();
        slots.add(new AvailabilitySlot(LocalDate.now().plusDays(1),LocalTime.of(12,0),LocalTime.of(15,0)));
        slots.add(new AvailabilitySlot(LocalDate.now().plusDays(1),LocalTime.of(16,0),LocalTime.of(18,0)));
        appointmentGroup.setAvailabilitySlots(slots);

        List<TimeSlot> generatedTimeSlots = TimeSlotUtility.generateTimeSlots(appointmentGroup);

        // verify count
        assertEquals(10,generatedTimeSlots.size(),"There should be 10 time slots generated");

        TimeSlot firstSlot = generatedTimeSlots.get(0);
        TimeSlot midSlot1 = generatedTimeSlots.get(5);
        TimeSlot midSlot2 = generatedTimeSlots.get(6);
        TimeSlot lastSlot = generatedTimeSlots.get(generatedTimeSlots.size()-1);
        
        assertEquals(1L,firstSlot.getAppointmentGroupId());
        assertEquals(LocalDate.now().plusDays(1),firstSlot.getDate());

        // first slot start and end time
        assertEquals(LocalTime.of(12,0),firstSlot.getStartTime());
        assertEquals(LocalTime.of(12,0).plusMinutes(20),firstSlot.getEndTime());

        // middle slot start and end time
        assertEquals(LocalTime.of(14,30), midSlot1.getStartTime());
        assertEquals(LocalTime.of(14,50), midSlot1.getEndTime());

        assertEquals(LocalTime.of(16,00), midSlot2.getStartTime());
        assertEquals(LocalTime.of(16,20), midSlot2.getEndTime());

        // last slot start and end time
        assertEquals(LocalTime.of(17,30), lastSlot.getStartTime());
        assertEquals(LocalTime.of(17,50), lastSlot.getEndTime());
        
        assertEquals(BookingStatus.AVAILABLE,firstSlot.getStatus());

    }

    @Test
    public void testGenerateTimeSlots_MultipleWindowDifferentDays(){

        AppointmentGroup appointmentGroup = new AppointmentGroup();
        appointmentGroup.setId(1L);
        appointmentGroup.setTitle("Test Appointment Goup");
        appointmentGroup.setType(AppointmentType.GROUP);
        appointmentGroup.setDurationPerSlot(20);
        appointmentGroup.setGapBetweenSlots(0);
        

        List<AvailabilitySlot> slots = new ArrayList<>();
        slots.add(new AvailabilitySlot(LocalDate.now().plusDays(1),LocalTime.of(12,0),LocalTime.of(14,0)));
        slots.add(new AvailabilitySlot(LocalDate.now().plusDays(2),LocalTime.of(12,0),LocalTime.of(14,0)));
        slots.add(new AvailabilitySlot(LocalDate.now().plusDays(3),LocalTime.of(12,0),LocalTime.of(14,0)));
        appointmentGroup.setAvailabilitySlots(slots);

        List<TimeSlot> generatedTimeSlots = TimeSlotUtility.generateTimeSlots(appointmentGroup);

        // verify count
        assertEquals(18,generatedTimeSlots.size(),"There should be 18 time slots generated");
        LocalDate base = LocalDate.now().plusDays(1);
        for (int i = 0; i < generatedTimeSlots.size(); i++) {
            LocalDate expected = base.plusDays(i / 6);
            assertEquals(expected, generatedTimeSlots.get(i).getDate(), "Incorrect Slot Date");
        }

    }
    
}
