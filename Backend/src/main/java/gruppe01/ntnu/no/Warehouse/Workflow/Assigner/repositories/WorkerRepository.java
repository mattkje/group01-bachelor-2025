package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {
}
