package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Timetable entity.
 */
@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    @Query("SELECT t FROM Timetable t WHERE FUNCTION('DATE', t.startTime) = :date")
    List<Timetable> findByStartDate(LocalDate date);

    @Query("SELECT t FROM Timetable t WHERE t.worker.id = :workerId AND :dateTime BETWEEN t.startTime AND t.endTime")
    List<Timetable> findByWorkerAndDateTime(Long workerId, LocalDateTime dateTime);

    @Query("SELECT t FROM Timetable t WHERE t.worker.id = :workerId AND FUNCTION('DATE', t.startTime) = FUNCTION('DATE', :dateTime)")
    List<Timetable> findWorkerTimetableForDay(Long workerId, LocalDateTime dateTime);

}