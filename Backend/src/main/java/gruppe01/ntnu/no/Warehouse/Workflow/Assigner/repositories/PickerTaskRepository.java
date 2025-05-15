package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PickerTask entity.
 */
@Repository
public interface PickerTaskRepository extends JpaRepository<PickerTask, Long> {
}
