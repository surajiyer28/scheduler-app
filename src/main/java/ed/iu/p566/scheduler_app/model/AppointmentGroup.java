package ed.iu.p566.scheduler_app.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    // new field to store gap between timeslots
    private int gapBetweenSlots = 0;

    @Column(columnDefinition = "TEXT")
    private String availabilitySlots;
    
    public List<AvailabilitySlot> getAvailabilitySlots() {
        if (availabilitySlots == null || availabilitySlots.isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(availabilitySlots.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(AvailabilitySlot::fromStorageString)
                .collect(Collectors.toList());
    }

    public void setAvailabilitySlots(List<AvailabilitySlot> slots) {
        if (slots == null || slots.isEmpty()) {
            this.availabilitySlots = "";
            return;
        }
        
        this.availabilitySlots = slots.stream()
                .map(AvailabilitySlot::stringConverter)
                .collect(Collectors.joining(";"));
    }


    public AppointmentGroup(Long professorId, String title, AppointmentType type, LocalDate startDate, LocalDate endDate, int durationMinutes) {
        this.professorId = professorId;
        this.title = title;
        this.type = type;
        this.durationPerSlot = durationMinutes;
        this.createdAt = LocalDateTime.now();
    }

}


