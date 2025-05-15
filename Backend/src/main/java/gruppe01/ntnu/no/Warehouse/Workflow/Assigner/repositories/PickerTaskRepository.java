package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PickerTask entity.
 */
@Repository
public interface PickerTaskRepository extends JpaRepository<PickerTask, Long> {

  @Query("SELECT p FROM PickerTask p WHERE p.zone.id = :zoneId")
  List<PickerTask> findByZoneId(@Param("zoneId") Long zoneId);

  @Query("""
    SELECT COUNT(p) FROM PickerTask p
    WHERE p.date = :date
      AND p.endTime IS NOT NULL
      AND p.zone.id = :zoneId
""")
  int countCompletedTasksByDateAndZone(@Param("date") LocalDate date, @Param("zoneId") Long zoneId);

  @Query("""
    SELECT p FROM PickerTask p
    WHERE p.date = :date
      AND p.endTime IS NULL
      AND (:zoneId = 0 OR p.zone.id = :zoneId)
""")
  List<PickerTask> findNotDoneTasksByDateAndZone(@Param("date") LocalDate date, @Param("zoneId") long zoneId);

  @Query("""
    SELECT COALESCE(SUM(p.packAmount), 0) FROM PickerTask p
    WHERE p.date = :date
      AND p.endTime IS NOT NULL
      AND p.zone.id = :zoneId
""")
  int sumPackAmountByDateAndZone(@Param("date") LocalDate date, @Param("zoneId") Long zoneId);

  List<PickerTask> findByDate(LocalDate date);

  List<PickerTask> findByDateAndEndTimeIsNull(LocalDate date);

  @Query("SELECT pt FROM PickerTask pt WHERE pt.zone.id = :zoneId AND pt.date = :today AND pt.endTime IS NULL")
  Set<PickerTask> findTodayUnfinishedPickerTasksByZoneId(@Param("zoneId") Long zoneId, @Param("today") LocalDate today);

  @Query("""
  SELECT COUNT(pt) FROM PickerTask pt
  WHERE pt.date = :date
    AND (:zoneId = 0 OR pt.zone.id = :zoneId)
""")
  int countPickerTasksByDateAndZone(@Param("date") LocalDate date, @Param("zoneId") Long zoneId);

  @Query("""
  SELECT pt FROM PickerTask pt
  WHERE pt.zone.id = :zoneId
    AND pt.time > 0
    AND pt.date > :oneWeekAgo
""")
  List<PickerTask> findValidPickerTasksByZoneSince(@Param("zoneId") Long zoneId,
                                                   @Param("oneWeekAgo") LocalDate oneWeekAgo);

  @Query("SELECT p FROM PickerTask p WHERE p.date = :date AND p.zone.id = :zoneId AND p.endTime IS NULL")
  Set<PickerTask> findUnfinishedPickerTasksByZoneIdAndDate(@Param("zoneId") Long zoneId, @Param("date") LocalDate date);

  @Query("SELECT p FROM PickerTask p WHERE p.date = :date AND (:zoneId = 0 OR p.zone.id = :zoneId)")
  Set<PickerTask> findAllByZoneIdAndDate(@Param("zoneId") Long zoneId, @Param("date") LocalDate date);

}
