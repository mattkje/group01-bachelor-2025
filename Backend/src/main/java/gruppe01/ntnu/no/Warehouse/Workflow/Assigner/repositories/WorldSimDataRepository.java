package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorldSimData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for WorldSimData entity.
 */
@Repository
public interface WorldSimDataRepository extends JpaRepository<WorldSimData, Long> {
}
