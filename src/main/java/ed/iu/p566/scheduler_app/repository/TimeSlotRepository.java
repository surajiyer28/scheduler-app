package ed.iu.p566.scheduler_app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ed.iu.p566.scheduler_app.model.TimeSlot;
import ed.iu.p566.scheduler_app.model.TimeSlot.BookingStatus;

@Repository
public interface TimeSlotRepository extends CrudRepository<TimeSlot, Long> {

    
    List<TimeSlot> findByAppointmentGroupId(Long appointmentGroupId);

    List<TimeSlot> findByAppointmentGroupIdOrderByDateAscStartTimeAsc(Long appointmentGroupId);

    boolean existsByAppointmentGroupIdAndBookedByUserId(Long appointmentGroupId, Long userId);

    List<TimeSlot> findByBookedByUserIdOrderByDateAscStartTimeAsc(Long userId);

    List<TimeSlot> findByBookedByUserIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(Long userId, LocalDate date);


    // method to check if a student is a part of any other group booking
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TimeSlot t " +
            "WHERE t.appointmentGroupId = :appointmentGroupId AND (t.bookedByUserId = :userId OR t.groupMemberIds LIKE CONCAT('%', :userId, '%'))")
    boolean isUserInAnyBookingInGroup(@Param("appointmentGroupId") Long appointmentGroupId, @Param("userId") Long userId);


    List<TimeSlot> findByStatusAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(BookingStatus status, LocalDate date);

}