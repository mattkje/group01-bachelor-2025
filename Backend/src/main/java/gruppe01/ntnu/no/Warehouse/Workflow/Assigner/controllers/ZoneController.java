package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/zones")
@Tag(name = "ZoneController", description = "Controller for managing zones")
public class ZoneController {

  private final ZoneService zoneService;

  public ZoneController(ZoneService zoneService) {
    this.zoneService = zoneService;
  }

  @Operation(
      summary = "Get all zones",
      description = "Retrieve a list of all zones in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of zones"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<List<Zone>> getAllZones() {
    return ResponseEntity.ok(zoneService.getAllZones());
  }

  @Operation(
      summary = "Get all task zones",
      description = "Retrieve a list of all task zones in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of task zones"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/task-zones")
  public ResponseEntity<List<Zone>> getAllTaskZones() {
    return ResponseEntity.ok(zoneService.getAllTaskZones());
  }

  @Operation(
      summary = "Get all picker zones",
      description = "Retrieve a list of all picker zones in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of picker zones"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/picker-zones")
  public ResponseEntity<List<Zone>> getAllPickerZones() {
    return ResponseEntity.ok(zoneService.getAllPickerZones());
  }

  @Operation(
      summary = "Get zone by ID",
      description = "Retrieve a zone by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved zone"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Zone> getZoneById(
      @Parameter(description = "ID of the zone to retrieve", required = true)
      @PathVariable Long id) {
    Zone zone = zoneService.getZoneById(id);
    if (zone != null) {
      return ResponseEntity.ok(zone);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  @Operation(
      summary = "Get all workers by zone ID",
      description = "Retrieve a list of all workers assigned to a specific zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of workers"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/workers")
  public ResponseEntity<Set<Worker>> getWorkersByZoneId(
      @Parameter(description = "ID of the zone to retrieve workers from", required = true)
      @PathVariable Long id) {
    Set<Worker> workers = zoneService.getWorkersByZoneId(id);
    if (workers == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } else {
      return ResponseEntity.ok(workers);
    }

  }

  @Operation(
      summary = "Get all tasks by zone ID",
      description = "Retrieve a list of all tasks assigned to a specific zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of tasks"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/tasks")
  public ResponseEntity<Set<Task>> getTasksByZoneId(
      @Parameter(description = "ID of the zone to retrieve tasks from", required = true)
      @PathVariable Long id) {
    Set<Task> tasks = zoneService.getTasksByZoneId(id);
    if (tasks == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } else {
      return ResponseEntity.ok(tasks);
    }
  }

  @Operation(
      summary = "Get all active tasks by zone ID",
      description = "Retrieve a list of all active tasks assigned to a specific zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of active tasks"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/active-tasks")
  public ResponseEntity<Set<ActiveTask>> getActiveTasksByZoneId(
      @Parameter(description = "ID of the zone to retrieve active tasks from", required = true)
      @PathVariable Long id) {
    Set<ActiveTask> activeTasks = zoneService.getActiveTasksByZoneId(id);
    if (activeTasks == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } else {
      return ResponseEntity.ok(activeTasks);
    }
  }

  @Operation(
      summary = "Get all picker tasks by zone ID",
      description = "Retrieve a list of all picker tasks assigned to a specific zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of picker tasks"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/picker-tasks")
  public ResponseEntity<Set<PickerTask>> getPickerTasksByZoneId(
      @Parameter(description = "ID of the zone to retrieve picker tasks from", required = true)
      @PathVariable Long id) {
    Set<PickerTask> pickerTasks = zoneService.getPickerTasksByZoneId(id);
    if (pickerTasks == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } else {
      return ResponseEntity.ok(pickerTasks);
    }
  }

  @Operation(
      summary = "Get all active tasks for today by zone ID",
      description = "Retrieve a list of all active tasks assigned to a specific zone for today."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of active tasks for today"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/active-tasks-now")
  public ResponseEntity<Set<ActiveTask>> getActiveTasksByZoneIdNow(
      @Parameter(description = "ID of the zone to retrieve active tasks from", required = true)
      @PathVariable Long id) {
    Set<ActiveTask> activeTasks = zoneService.getTodayUnfinishedTasksByZoneId(id);
    if (activeTasks == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } else {
      return ResponseEntity.ok(activeTasks);
    }
  }

  @Operation(
      summary = "Get all picker tasks for today by zone ID",
      description = "Retrieve a list of all picker tasks assigned to a specific zone for today."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of picker tasks for today"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/picker-tasks-now")
  public ResponseEntity<Set<PickerTask>> getPickerTasksByZoneIdNow(
      @Parameter(description = "ID of the zone to retrieve picker tasks from", required = true)
      @PathVariable Long id) {
    Set<PickerTask> pickerTasks = zoneService.getTodayUnfinishedPickerTasksByZoneId(id);
    if (pickerTasks == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } else {
      return ResponseEntity.ok(pickerTasks);
    }
  }

  @Operation(
      summary = "Get all active tasks for a specific day by zone ID",
      description = "Retrieve a list of all active tasks assigned to a specific zone for a specific day."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of active tasks for the day"),
      @ApiResponse(responseCode = "400", description = "Invalid date format"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/active-tasks/{date}")
  public ResponseEntity<Set<ActiveTask>> getActiveTasksByZoneIdAndDate(
      @Parameter(description = "ID of the zone to retrieve active tasks from", required = true)
      @PathVariable Long id,
      @Parameter(description = "Date to filter tasks by (format: yyyy-MM-dd)", required = true)
      @PathVariable String date) {
    try {
      LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
      Set<ActiveTask> activeTasks = zoneService.getAllTasksByZoneIdAndDate(id, parsedDate);
      if (activeTasks == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      } else {
        return ResponseEntity.ok(activeTasks);
      }
    } catch (DateTimeParseException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
  }

  @Operation(
      summary = "Get all picker tasks for a specific day by zone ID",
      description = "Retrieve a list of all picker tasks assigned to a specific zone for a specific day."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved list of picker tasks for the day"),
      @ApiResponse(responseCode = "400", description = "Invalid date format"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/picker-tasks/{date}")
  public ResponseEntity<Set<PickerTask>> getPickerTasksByZoneIdAndDate(
      @Parameter(description = "ID of the zone to retrieve picker tasks from", required = true)
      @PathVariable Long id,
      @Parameter(description = "Date to filter tasks by (format: yyyy-MM-dd)", required = true)
      @PathVariable String date) {
    try {
      LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
      Set<PickerTask> pickerTasks =
          zoneService.getAllPickerTasksByZoneIdAndDate(id, parsedDate);
      if (pickerTasks == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      } else {
        return ResponseEntity.ok(pickerTasks);
      }
    } catch (DateTimeParseException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
  }


  @Operation(
      summary = "Get number of tasks by zone ID by a specific date",
      description = "Retrieve the number of tasks assigned to a specific zone by the given date."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved number of tasks"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{zoneId}/{date}")
  public ResponseEntity<Integer> getNumberOfTasksByDateByZone(
      @Parameter(description = "ID of the zone to retrieve tasks from", required = true)
      @PathVariable Long zoneId,
      @Parameter(description = "Date to filter tasks by", required = true)
      @PathVariable LocalDate date) {
    if (zoneService.getZoneById(zoneId) != null) {
      return ResponseEntity.ok(zoneService.getNumberOfTasksForTodayByZone(zoneId, date));
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
  }

  @Operation(
      summary = "Get the minimum amount of time for active tasks by zone ID",
      description = "Retrieve the number of active tasks assigned to a specific zone for today."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved number of active tasks"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{zoneId}/active-tasks-now-min-time")
  public ResponseEntity<Integer> getMinTimeForActiveTasksByZoneIdNow(
      @Parameter(description = "ID of the zone to retrieve total min time of all active tasks from", required = true)
      @PathVariable Long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } else {
      return ResponseEntity.ok(zoneService.getMinTimeForActiveTasksByZoneIdNow(zoneId));
    }
  }

  @Operation(
      summary = "Add a new zone",
      description = "Add a new zone to the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully added new zone"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping
  public ResponseEntity<Zone> addZone(
      @Parameter(description = "Zone object to be added", required = true)
      @RequestBody Zone zone) {
    if (zone == null || zone.getName() == null || zone.getName().isEmpty() ||
        zone.getCapacity() <= 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
    return new ResponseEntity<>(zoneService.addZone(zone), HttpStatus.CREATED);
  }

  @Operation(
      summary = "Update an existing zone",
      description = "Update an existing zone in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated zone"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<Zone> updateZone(
      @Parameter(description = "ID of the zone to update", required = true)
      @PathVariable Long id,
      @Parameter(description = "Zone object with updated data", required = true)
      @RequestBody Zone zone) {
    if (zone == null || zone.getName() == null || zone.getName().isEmpty() ||
        zone.getCapacity() <= 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
    if (zoneService.getZoneById(id) == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return new ResponseEntity<>(zoneService.updateZone(id, zone), HttpStatus.OK);
  }

  @Operation(
      summary = "Update the picker zone status",
      description = "Update the picker zone status of a specific zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated picker zone status"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/is-picker-zone/{id}")
  public ResponseEntity<Zone> updatePickerZone(
      @Parameter(description = "ID of the zone to update", required = true)
      @PathVariable Long id) {
    if (zoneService.getZoneById(id) == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.ok(zoneService.changeIsPickerZone(id));
  }

  @Operation(
      summary = "Delete a zone",
      description = "Delete a zone from the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted zone"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Zone> deleteZone(
      @Parameter(description = "ID of the zone to delete", required = true)
      @PathVariable Long id) {
    if (zoneService.getZoneById(id) == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.ok(zoneService.deleteZone(id));
  }

  @Operation(
      summary = "Update the machine learning model",
      description = "Update the machine learning model for all zones."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated machine learning model"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/update-machine-learning-model")
  public void updateMachineLearningModel() throws IOException {
    zoneService.updateMachineLearningModel(LocalDateTime.now());
  }
}
