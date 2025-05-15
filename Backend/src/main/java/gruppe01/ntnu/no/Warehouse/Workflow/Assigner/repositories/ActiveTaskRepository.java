package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ActiveTask entity.
 */
@Repository
public interface ActiveTaskRepository extends JpaRepository<ActiveTask, Long> {

  @EntityGraph(attributePaths = {"task", "workers", "workers.licenses", "task.requiredLicense"})
  @Query("SELECT at FROM ActiveTask at")
  List<ActiveTask> findAllWithTasksAndWorkersAndLicenses();
}
