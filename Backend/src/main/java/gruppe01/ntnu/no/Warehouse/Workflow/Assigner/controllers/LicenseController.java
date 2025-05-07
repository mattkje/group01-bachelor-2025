package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.LicenseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LicenseController handles HTTP requests related to licenses.
 * It provides endpoints to create, read, update, and delete licenses.
 */
@RestController
@RequestMapping("/api/licenses")
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

    @GetMapping
    public List<License> getAllLicenses() {
        return licenseService.getAllLicenses();
    }

    @GetMapping("/{id}")
    public License getLicenseById(@PathVariable Long id) {
        return licenseService.getLicenseById(id);
    }

    @PostMapping
    public License createLicense(@RequestBody License license) {
        return licenseService.createLicense(license);
    }

    @PutMapping("/{id}")
    public License updateLicense(@PathVariable Long id, @RequestBody License license) {
        return licenseService.updateLicense(id, license);
    }

    @DeleteMapping("/{id}")
    public License deleteLicense(@PathVariable Long id) {
        return licenseService.deleteLicense(id);
    }
}
