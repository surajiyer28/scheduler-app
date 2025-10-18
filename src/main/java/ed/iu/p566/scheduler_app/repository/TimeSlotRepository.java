package ed.iu.p566.scheduler_app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ed.iu.p566.scheduler_app.model.TimeSlot;

@Repository
public interface TimeSlotRepository extends CrudRepository<TimeSlot, Long> {

    
    List<TimeSlot> findByAppointmentGroupId(Long appointmentGroupId);

    List<TimeSlot> findByAppointmentGroupIdOrderByDateAscStartTimeAsc(Long appointmentGroupId);

    boolean existsByAppointmentGroupIdAndBookedByUserId(Long appointmentGroupId, Long userId);

    List<TimeSlot> findByBookedByUserIdOrderByDateAscStartTimeAsc(Long userId);

    List<TimeSlot> findByBookedByUserIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(Long userId, LocalDate date);

}