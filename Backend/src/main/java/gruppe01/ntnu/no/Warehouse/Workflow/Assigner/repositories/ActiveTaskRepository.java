package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Repository interface for ActiveTask entity.
 */
@Repository
public interface ActiveTaskRepository extends JpaRepository<ActiveTask, Long> {

  @EntityGraph(attributePaths = {"task", "workers", "workers.licenses", "task.requiredLicense"})
  @Query("SELECT at FROM ActiveTask at")
  List<ActiveTask> findAllWithTasksAndWorkersAndLicenses();

  List<ActiveTask> findByDateAndEndTimeIsNull(LocalDate date);

  @Query("SELECT a FROM ActiveTask a WHERE a.date = :date AND a.endTime IS NULL AND a.task.zone.id = :taskZoneId")
  List<ActiveTask> findByDateAndEndTimeIsNullAndTask_Zone_Id(LocalDate date, Long taskZoneId);

  List<ActiveTask> findByDate(LocalDate date);

  List<ActiveTask> findByEndTimeIsNotNull();

  @Query("""
    SELECT a FROM ActiveTask a
    WHERE a.endTime IS NULL
      AND a.date = :date
      AND (:zoneId = 0 OR a.task.zone.id = :zoneId)
""")
  List<ActiveTask> findNotCompletedTasksByDateAndZone(@Param("date") LocalDate date, @Param("zoneId") Long zoneId);

  List<ActiveTask> findByStartTimeIsNullAndEndTimeIsNull();

  List<ActiveTask> findByStartTimeIsNotNullAndEndTimeIsNull();

  @Query("""
    SELECT COUNT(a) FROM ActiveTask a
    WHERE a.date = :date
      AND a.endTime IS NOT NULL
      AND a.task.zone.id = :zoneId
""")
  int countCompletedTasksByDateAndZone(@Param("date") LocalDate date, @Param("zoneId") Long zoneId);

  @Query("SELECT at FROM ActiveTask at WHERE at.task.zone.id = :zoneId")
  Set<ActiveTask> findActiveTasksByZoneId(@Param("zoneId") Long zoneId);

  @Query("SELECT at FROM ActiveTask at WHERE at.task.zone.id = :zoneId AND at.date = :today AND at.endTime IS NULL")
  Set<ActiveTask> findTodayUnfinishedTasksByZoneId(@Param("zoneId") Long zoneId, @Param("today") LocalDate today);

  @Query("""
  SELECT COUNT(at) FROM ActiveTask at
  WHERE at.date = :date
    AND (:zoneId = 0 OR at.task.zone.id = :zoneId)
""")
  int countActiveTasksByDateAndZone(@Param("date") LocalDate date, @Param("zoneId") Long zoneId);

  @Query("""
  SELECT COALESCE(SUM(t.minDuration), 0) 
  FROM ActiveTask at 
  JOIN at.task t
  WHERE t.zone.id = :zoneId
    AND at.endTime IS NULL
""")
  Integer sumMinTimeOfUnfinishedActiveTasksByZone(@Param("zoneId") Long zoneId);

  @Query("SELECT a FROM ActiveTask a WHERE a.date = :date AND a.task.zone.id = :zoneId AND a.endTime IS NULL")
  Set<ActiveTask> findUnfinishedTasksByZoneIdAndDate(@Param("zoneId") Long zoneId, @Param("date") LocalDate date);

  @Query("SELECT a FROM ActiveTask a WHERE a.date = :date AND (:zoneId = 0 OR a.task.zone.id = :zoneId)")
  Set<ActiveTask> findAllByZoneIdAndDate(@Param("zoneId") Long zoneId, @Param("date") LocalDate date);

}
