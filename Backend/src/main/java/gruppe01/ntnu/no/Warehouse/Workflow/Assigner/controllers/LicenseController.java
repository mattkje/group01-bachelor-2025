package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.License;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LicenseController handles HTTP requests related to licenses.
 * It provides endpoints to create, read, update, and delete licenses.
 */
@RestController
@RequestMapping("/api/licenses")
@Tag(name = "LicenseController", description = "Controller for managing licenses")
public class LicenseController {


  private final LicenseService licenseService;

  /**
   * Constructor for LicenseController.
   *
   * @param licenseService The service to handle license operations.
   */
  public LicenseController(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  @Operation(
      summary = "Get all licenses",
      description = "Retrieve a list of all licenses in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved licenses"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<List<License>> getAllLicenses() {
    return ResponseEntity.ok(licenseService.getAllLicenses());
  }

  /**
   * Retrieves a license by its ID.
   *
   * @param id the ID of the license
   * @return the license with the specified ID, or null if not found
   */
  @Operation(
      summary = "Get license by ID",
      description = "Retrieve a license by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved license"),
      @ApiResponse(responseCode = "404", description = "License not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Optional<License>> getLicenseById(
      @Parameter(description = "ID of the license to retrieve")
      @PathVariable Long id) {
    Optional<License> license = Optional.ofNullable(licenseService.getLicenseById(id));
    if (license.isPresent()) {
      return ResponseEntity.ok(license);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty());
    }
  }

  /**
   * Creates a new license.
   *
   * @param license the license to create
   * @return the created license
   */
  @Operation(
      summary = "Create a new license",
      description = "Create a new license in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully created license"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping
  public ResponseEntity<License> createLicense(
      @Parameter(description = "License object to create")
      @RequestBody License license) {
    if (license.getName().isEmpty() || license.getName().isBlank()) {
      return ResponseEntity.badRequest().build();
    } else {
      return ResponseEntity.ok(licenseService.createLicense(license));
    }
  }

  /**
   * Updates an existing license.
   *
   * @param id      the ID of the license to update
   * @param license the updated license data
   * @return the updated license
   */
  @Operation(
      summary = "Update a license",
      description = "Update an existing license in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated license"),
      @ApiResponse(responseCode = "404", description = "License not found"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<License> updateLicense(
      @Parameter(description = "ID of the license to update")
      @PathVariable Long id,
      @Parameter(description = "License object to update")
      @RequestBody License license) {
    if (licenseService.getLicenseById(id) == null) {
      return ResponseEntity.notFound().build();
    } else if (license.getName().isEmpty() || license.getName().isBlank()) {
      return ResponseEntity.badRequest().build();
    } else {
      return ResponseEntity.ok(licenseService.updateLicense(id, license));
    }
  }

  /**
   * Deletes a license by its ID.
   *
   * @param id the ID of the license to delete
   * @return the deleted license, or null if not found
   */
  @Operation(
      summary = "Delete a license",
      description = "Delete a license by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted license"),
      @ApiResponse(responseCode = "404", description = "License not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<License> deleteLicense(
      @Parameter(description = "ID of the license to delete")
      @PathVariable Long id) {
    if (licenseService.getLicenseById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(licenseService.deleteLicense(id));
    }
  }
}
