package gruppe01.ntnu.no.warehouse.workflow.assigner.repositories;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Timetable;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT t FROM Timetable t WHERE FUNCTION('DATE', t.startTime) = :date ORDER BY t.startTime ASC")
    List<Timetable> findByStartDateSortedByTime(LocalDate date);

    @Query("SELECT t FROM Timetable t WHERE t.worker.id = :workerId AND :dateTime BETWEEN t.startTime AND t.endTime")
    List<Timetable> findByWorkerAndDateTime(Long workerId, LocalDateTime dateTime);

    @Query("SELECT t FROM Timetable t WHERE t.worker.id = :workerId AND FUNCTION('DATE', t.startTime) = FUNCTION('DATE', :dateTime)")
    List<Timetable> findWorkerTimetableForDay(Long workerId, LocalDateTime dateTime);

    @Query("SELECT t FROM Timetable t WHERE FUNCTION('DATE', t.realStartTime) = :today")
    List<Timetable> findByRealStartDate(@Param("today") LocalDate today);

    @Query("""
    SELECT t FROM Timetable t
    WHERE t.worker.zone = :zoneId
      AND FUNCTION('DATE', t.realStartTime) = :today
""")
    List<Timetable> findTodayTimetablesByZone(@Param("zoneId") Long zoneId, @Param("today") LocalDate today);

    @Query("SELECT t FROM Timetable t WHERE t.worker.zone = :zoneId")
    List<Timetable> findAllByZoneId(@Param("zoneId") Long zoneId);

    @Query("""
    SELECT t FROM Timetable t
    WHERE t.realStartTime IS NOT NULL
      AND FUNCTION('DATE', t.realStartTime) = :day
      AND (:zoneId = 0 OR t.worker.zone = :zoneId)
""")
    List<Timetable> findByDayAndZone(@Param("day") LocalDate day, @Param("zoneId") Long zoneId);

    @Query("""
    SELECT t FROM Timetable t
    WHERE t.realStartTime IS NOT NULL
      AND t.realEndTime IS NOT NULL
      AND t.realStartTime < :day
      AND t.realEndTime > :day
      AND (:zoneId = 0 OR t.worker.zone = :zoneId)
""")
    List<Timetable> findTimetablesOfWorkersWorkingByZone(@Param("day") LocalDateTime day, @Param("zoneId") Long zoneId);

    @Query("""
    SELECT t FROM Timetable t
    WHERE t.startTime >= :startDateTime
      AND t.startTime < :endDateTime
      AND t.worker.zone = :zoneId
""")
    List<Timetable> findTimetablesForOneWeek(@Param("startDateTime") LocalDateTime startDateTime,
                                             @Param("endDateTime") LocalDateTime endDateTime,
                                             @Param("zoneId") Long zoneId);

    @Modifying
    @Query("DELETE FROM Timetable t WHERE t.worker.id = :workerId")
    void deleteAllByWorkerId(@Param("workerId") Long workerId);
}