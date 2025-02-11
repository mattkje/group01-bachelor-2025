package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
