package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Worker entity.
 */
@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {

  @EntityGraph(attributePaths = "licenses")
  @Query("SELECT w FROM Worker w")
  List<Worker> findAllWithLicenses();

  @Query("SELECT w FROM Worker w WHERE w.availability = true")
  List<Worker> findAvailableWorkers();

  @Query("SELECT w FROM Worker w WHERE w.availability = false")
  List<Worker> findUnavailableWorkers();

}
