package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetables")
public class TimetableController {

    @Autowired
    private TimetableService timetableService;

    @GetMapping
    public List<Timetable> getTimetables() {
        return timetableService.getAllTimetables();
    }

    @GetMapping("/today/zone/{zoneId}")
    public List<Timetable> getTodaysTimetable(@PathVariable Long zoneId) {
        return timetableService.getTodaysTimetablesByZone(zoneId);
    }

    @GetMapping("zone/{zoneId}")
    public List<Timetable> getAllTimetablesByZone(@PathVariable Long zoneId) {
        return timetableService.getAllTimetablesByZone(zoneId);
    }

    @GetMapping("{day}/zone/{zoneId}")
    public List<Timetable> getTimetablesByDayAndZone(@PathVariable LocalDateTime day, @PathVariable Long zoneId) {
        return timetableService.getTimetablesByDayAndZone(day, zoneId);
    }

    @GetMapping("/one-week/{date}/{zoneId}")
    public List<Timetable> getTimetablesForOneWeek(@PathVariable LocalDate date, @PathVariable Long zoneId) {
        return timetableService.getTimetablesForOneWeek(date, zoneId);
    }

    @PutMapping("/{id}/set-start-time")
    public Timetable setStartTime(@PathVariable Long id) {
        return timetableService.setStartTime(id);
    }

    @PutMapping("/{id}")
    public Timetable updateTimetable(@PathVariable Long id, @RequestBody Timetable timetable) {
        return timetableService.updateTimetable(id, timetable);
    }

    @PostMapping("/{workerId}")
    public Timetable addTimetable(@RequestBody Timetable timetable, @PathVariable Long workerId) {
        return timetableService.addTimetable(timetable, workerId);
    }

    @DeleteMapping("/{id}")
    public void deleteTimetable(@PathVariable Long id) {
        timetableService.deleteTimetable(id);
    }
}
