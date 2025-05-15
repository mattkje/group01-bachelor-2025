package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {


  @Query("SELECT e FROM Notification e ORDER BY e.time DESC LIMIT 1")
  Notification findTopByOrderByTimeDesc();

  @Query("SELECT e FROM Notification e WHERE e.zoneId = ?1 ORDER BY e.time DESC LIMIT 1")
  Notification findTopByZoneIdOrderByTimeDesc(long zoneId);
}
