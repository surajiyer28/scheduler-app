package ed.iu.p566.scheduler_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ed.iu.p566.scheduler_app.model.AppointmentGroup;


@Repository
public interface AppointmentGroupRepository extends CrudRepository<AppointmentGroup, Long> {

    List<AppointmentGroup> getAppointmentGroupsByProfessorId(Long professorId);

    Optional<AppointmentGroup> findById(Long id);
}
