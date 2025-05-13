package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ErrorMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorMessageRepository extends JpaRepository<ErrorMessage, Long> {


   @Query("SELECT e FROM ErrorMessage e ORDER BY e.time DESC LIMIT 1")
   ErrorMessage findTopByOrderByTimeDesc();

    @Query("SELECT e FROM ErrorMessage e WHERE e.zoneId = ?1 ORDER BY e.time DESC LIMIT 1")
    ErrorMessage findTopByZoneIdOrderByTimeDesc(long zoneId);
}
