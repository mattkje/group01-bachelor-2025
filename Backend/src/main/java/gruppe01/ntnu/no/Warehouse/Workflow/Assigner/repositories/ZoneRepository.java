package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Zone entity.
 */
@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    @EntityGraph(attributePaths = {"workers", "workers.licenses", "tasks", "tasks.requiredLicense"})
    @Query("SELECT DISTINCT z FROM Zone z")
    List<Zone> findAllWithTasksAndWorkersAndLicenses();
}
