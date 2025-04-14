package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import jakarta.persistence.Entity;
import org.hibernate.jdbc.Work;
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
}
