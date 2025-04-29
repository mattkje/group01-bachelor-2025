package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorldSimData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for WorldSimData entity.
 */
@Repository
public interface WorldSimDataRepository extends JpaRepository<WorldSimData, Long> {

    @Query("SELECT w FROM WorldSimData w WHERE w.zone = :zone ORDER BY w.time DESC LIMIT 1")
    Optional<WorldSimData> findFirstByZoneOrderByTimeDesc(@Param("zone") Zone zone);
}
