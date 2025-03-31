package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
