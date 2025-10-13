package ed.iu.p566.scheduler_app.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ed.iu.p566.scheduler_app.model.TimeSlot;

@Repository
public interface TimeSlotRepository extends CrudRepository<TimeSlot, Long> {

    
    List<TimeSlot> findByAppointmentGroupId(Long appointmentGroupId);

    List<TimeSlot> findByAppointmentGroupIdOrderByDateAscStartTimeAsc(Long appointmentGroupId);

}