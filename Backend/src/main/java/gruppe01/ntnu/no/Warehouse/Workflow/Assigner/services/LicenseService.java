package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.LicenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing licenses.
 */
@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;

    /**
     * Constructor for LicenseService.
     *
     * @param licenseRepository the repository for License entity
     */
    public LicenseService(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    /**
     * Retrieves all licenses from the repository.
     *
     * @return a list of all licenses
     */
    public List<License> getAllLicenses() {
        return licenseRepository.findAll();
    }

    /**
     * Retrieves a license by its ID.
     *
     * @param id the ID of the license
     * @return the license with the specified ID, or null if not found
     */
    public License getLicenseById(Long id) {
        return licenseRepository.findById(id).orElse(null);
    }

    /**
     * Creates a new license.
     *
     * @param license the license to create
     * @return the created license
     */
    public License createLicense(License license) {
        return licenseRepository.save(license);
    }

    /**
     * Deletes a license by its ID.
     *
     * @param id the ID of the license to delete
     * @return the deleted license, or null if not found
     */
    public License deleteLicense(Long id) {
        License license = licenseRepository.findById(id).orElse(null);
        if (license != null) {
            licenseRepository.delete(license);
        }
        return license;
    }

    /**
     * Updates an existing license.
     *
     * @param id      the ID of the license to update
     * @param license the updated license data
     * @return the updated license
     */
    public License updateLicense(Long id, License license) {
        if (licenseRepository.findById(id).isPresent()) {
            License updatedLicense = licenseRepository.findById(id).get();
            updatedLicense.setName(license.getName());
            updatedLicense.setWorkers(license.getWorkers());
            return licenseRepository.save(updatedLicense);
        } else {
            return null;
        }
    }
}
