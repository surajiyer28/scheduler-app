package ed.iu.p566.scheduler_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ed.iu.p566.scheduler_app.model.User;
import ed.iu.p566.scheduler_app.model.User.UserRole;

@Repository
public interface UserRepository extends CrudRepository<User,Long>{

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.id != :currentUserId ORDER BY u.name")
    List<User> findAllStudentsExcept(@Param("currentUserId") Long currentUserId);

}
