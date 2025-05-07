package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.MonteCarloData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for MonteCarloData entity.
 */
@Repository
public interface MonteCarloDataRepository extends JpaRepository<MonteCarloData, Long> {
    @Query("SELECT m FROM MonteCarloData m WHERE m.zoneId = :zoneId ORDER BY m.time DESC LIMIT 1")
    MonteCarloData findLastByZoneId(@Param("zoneId") long zoneId);
}
