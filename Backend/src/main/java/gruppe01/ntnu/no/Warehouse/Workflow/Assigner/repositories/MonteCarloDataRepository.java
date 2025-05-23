package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import java.util.List;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.MonteCarloData;
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


    @Query("SELECT m FROM MonteCarloData m WHERE m.zoneId = :zoneId ORDER BY m.time ASC ")
    List<MonteCarloData> findAllByZoneId(@Param("zoneId") long zoneId);
}
