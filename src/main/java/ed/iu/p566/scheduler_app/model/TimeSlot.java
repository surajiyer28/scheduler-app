package ed.iu.p566.scheduler_app.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.*;
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
@Table(name = "time_slots")
public class TimeSlot {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long appointmentGroupId;


    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    public enum BookingStatus {
        AVAILABLE,
        BOOKED,
        CANCELLED
    }

    @Enumerated(EnumType.STRING)
    @NotNull
    private BookingStatus status;

    private Long bookedByUserId;

    @Column(columnDefinition = "TEXT")
    private String groupMemberIds;

    @Transient
    private String bookedByUserName;
    
    @Transient
    private String bookedByUserEmail;

    public List<Long> getGroupMemberIdsList() {

        if (groupMemberIds == null || groupMemberIds.isEmpty()) {

            return new ArrayList<>();
        }
        
        return Arrays.stream(groupMemberIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

    }

    public void setGroupMemberIdsList(List<Long> groupIds) {

        if (groupIds == null || groupIds.isEmpty()) {
            this.groupMemberIds = "";
            return;

        }
        
        this.groupMemberIds = groupIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

    }

    public void addGroupMember(Long memberId) {

        List<Long> group = getGroupMemberIdsList();

        if (!group.contains(memberId)) {
            group.add(memberId);
            setGroupMemberIdsList(group);
        }

    }

    public TimeSlot(Long appointmentGroupId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.appointmentGroupId = appointmentGroupId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = BookingStatus.AVAILABLE;
    }


}