package ed.iu.p566.scheduler_app.model;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySlot {

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public boolean checkOverlap(AvailabilitySlot other) {
        if (!this.date.equals(other.date)) {
            return false; 
        }
        
        return this.startTime.isBefore(other.endTime) && 
               this.endTime.isAfter(other.startTime);
    }

    public String stringConverter() {
        return date + "|" + startTime + "|" + endTime;
    }
    
    public static AvailabilitySlot fromStorageString(String str) {
        String[] parts = str.split("\\|");
        return new AvailabilitySlot(
            LocalDate.parse(parts[0]),
            LocalTime.parse(parts[1]),
            LocalTime.parse(parts[2])
        );
    }
}
