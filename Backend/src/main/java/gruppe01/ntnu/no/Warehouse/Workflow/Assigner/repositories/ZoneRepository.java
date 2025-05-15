package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Zone entity.
 */
@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    @EntityGraph(attributePaths = {"workers", "workers.licenses", "tasks", "tasks.requiredLicense"})
    @Query("SELECT DISTINCT z FROM Zone z")
    List<Zone> findAllWithTasksAndWorkersAndLicenses();

    @Query("SELECT z FROM Zone z WHERE z.isPickerZone = false")
    List<Zone> findAllNonPickerZones();

    @Query("SELECT z FROM Zone z WHERE z.isPickerZone = true")
    List<Zone> findAllPickerZones();

}
