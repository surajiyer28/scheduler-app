package ed.iu.p566.scheduler_app.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointment_groups")
public class AppointmentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    private Long professorId;

    
    private LocalDateTime createdAt;

    @NotBlank(message = "Title is required")
    private String title;

    public enum AppointmentType {
        GROUP,
        INDIVIDUAL
    }

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Appointment type is required")
    private AppointmentType type;

    @NotNull
    private int durationPerSlot;

    @NotNull(message = "Atleast one date is required")
    private String dates;

    @NotNull(message = "Atleast one time is required")
    private String startTimes;

    @NotNull(message = "Atleast one time is required")
    private String endTimes;

    public void setDates(String[] datesArray) {
        this.dates = String.join(",", datesArray);
    }
    
    public void setStartTimes(String[] startTimesArray) {
        this.startTimes = String.join(",", startTimesArray);
    }
    
    public void setEndTimes(String[] endTimesArray) {
        this.endTimes = String.join(",", endTimesArray);
    }

    public AppointmentGroup(Long professorId, String title, AppointmentType type, LocalDate startDate, LocalDate endDate, int durationMinutes) {
        this.professorId = professorId;
        this.title = title;
        this.type = type;
        this.durationPerSlot = durationMinutes;
        this.createdAt = LocalDateTime.now();
    }

}


