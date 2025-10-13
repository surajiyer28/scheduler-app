package ed.iu.p566.scheduler_app.utilities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import ed.iu.p566.scheduler_app.model.AppointmentGroup;
import ed.iu.p566.scheduler_app.model.AvailabilitySlot;
import ed.iu.p566.scheduler_app.model.TimeSlot;

public class TimeSlotUtility {
    

    public static List<TimeSlot> generateTimeSlots(AppointmentGroup appointmentGroup) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        
        if (appointmentGroup == null || appointmentGroup.getId() == null) {
            return timeSlots;
        }
        
        List<AvailabilitySlot> availabilitySlots = appointmentGroup.getAvailabilitySlots();
        int durationPerSlot = appointmentGroup.getDurationPerSlot();
        Long appointmentGroupId = appointmentGroup.getId();
        
        for (AvailabilitySlot availabilitySlot : availabilitySlots) {
            LocalDate date = availabilitySlot.getDate();
            LocalTime windowStart = availabilitySlot.getStartTime();
            LocalTime windowEnd = availabilitySlot.getEndTime();
            
            LocalTime slotStart = windowStart;
            

            while (slotStart.plusMinutes(durationPerSlot).isBefore(windowEnd) || slotStart.plusMinutes(durationPerSlot).equals(windowEnd)) {
                
                LocalTime slotEnd = slotStart.plusMinutes(durationPerSlot);
                
                TimeSlot timeSlot = new TimeSlot(appointmentGroupId, date, slotStart, slotEnd);
                
                timeSlots.add(timeSlot);
                
                slotStart = slotEnd;
            }
        }
        
        return timeSlots;
    }
}