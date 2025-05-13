package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ErrorMessage;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ErrorMessageService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/error-messages")
@Tag(name = "ErrorMessageController", description = "Controller for managing error messages")
public class ErrorMessageController {

    private final ErrorMessageService errorMessageService;
    private final ZoneService zoneService;

    public ErrorMessageController(ErrorMessageService errorMessageService, ZoneService zoneService) {
        this.errorMessageService = errorMessageService;
        this.zoneService = zoneService;
    }

    @Operation(
            summary = "Get all error messages",
            description = "Retrieve a list of all error messages in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved error messages"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ErrorMessage>> getAllErrorMessages() {
        return ResponseEntity.ok(errorMessageService.getAllErrorMessages());
    }

    @Operation(
            summary = "Get error message by ID",
            description = "Retrieve an error message by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved error message"),
            @ApiResponse(responseCode = "404", description = "Error message not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ErrorMessage> getErrorMessageById(
            @Parameter(description = "ID of the error message to retrieve")
            @PathVariable long id) {
        if (errorMessageService.getErrorMessageById(id) == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(errorMessageService.getErrorMessageById(id));
        }
    }

    @Operation(
            summary = "Get error messages by zone ID",
            description = "Retrieve a list of error messages associated with a specific zone ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved error messages"),
            @ApiResponse(responseCode = "404", description = "No error messages found for the specified zone ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<ErrorMessage>> getErrorMessagesByZoneId(
            @Parameter(description = "ID of the zone to retrieve error messages for")
            @PathVariable long zoneId) {
        List<ErrorMessage> errorMessages = errorMessageService.getErrorMessagesByZoneId(zoneId);
        if (zoneService.getZoneById(zoneId) == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(errorMessages);
        }
    }

    @Operation(
            summary = "Update an already existing error message",
            description = "Update an existing error message in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated error message"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Error message not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ErrorMessage> updateErrorMessage(
            @Parameter(description = "ID of the error message to update")
            @PathVariable long id,
            @Parameter(description = "Updated error message object")
            @RequestBody ErrorMessage errorMessage) {
        if (errorMessageService.getErrorMessageById(id) == null) {
            return ResponseEntity.notFound().build();
        } else if (errorMessage.getTime() == null || errorMessage.getMessage().isEmpty() ||
                errorMessage.getMessage().isBlank()) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(errorMessageService.updateErrorMessage(id, errorMessage));
        }
    }

    @Operation(
            summary = "Create a new error message",
            description = "Create a new error message in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created error message"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Zone not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{zoneId}")
    public ResponseEntity<ErrorMessage> createErrorMessage(
            @Parameter(description = "ID of the zone to create an error message for")
            @PathVariable long zoneId,
            @Parameter(description = "New error message object")
            @RequestBody ErrorMessage errorMessage) {
        if (errorMessage.getTime() == null || errorMessage.getMessage().isEmpty() ||
                errorMessage.getMessage().isBlank()) {
            return ResponseEntity.badRequest().build();
        } else if (zoneService.getZoneById(zoneId) == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(errorMessageService.createErrorMessage(zoneId, errorMessage));
        }
    }

    @Operation(
            summary = "Delete an error message",
            description = "Delete an error message by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted error message"),
            @ApiResponse(responseCode = "404", description = "Error message not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ErrorMessage> deleteErrorMessage(
            @Parameter(description = "ID of the error message to delete")
            @PathVariable long id) {
        if (errorMessageService.getErrorMessageById(id) == null) {
            return ResponseEntity.notFound().build();
        } else {
            errorMessageService.deleteErrorMessage(id);
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/done-by")
    public ResponseEntity<Map<String, String>> getLastErrorMessageTime() {
        String lastErrorMessageTime = errorMessageService.getLastErrorMessageTime();
        if (lastErrorMessageTime == null) {
            return ResponseEntity.notFound().build();
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("time", lastErrorMessageTime);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/done-by/{zoneId}")
    public ResponseEntity<Map<String, String>> getLastErrorMessageTimeByZone(@PathVariable long zoneId) {
        String lastErrorMessageTime = errorMessageService.getErrorMessageTimeByZone(zoneId);
        if (lastErrorMessageTime == null) {
            return ResponseEntity.notFound().build();
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("time", lastErrorMessageTime);
            return ResponseEntity.ok(response);
        }
    }


}
