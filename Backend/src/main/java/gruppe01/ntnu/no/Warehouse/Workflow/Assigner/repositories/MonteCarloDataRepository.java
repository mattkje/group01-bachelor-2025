package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.MonteCarloData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for MonteCarloData entity.
 */
@Repository
public interface MonteCarloDataRepository extends JpaRepository<MonteCarloData, Long> {
}
