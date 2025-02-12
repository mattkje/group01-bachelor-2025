package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    public List<License> getAllLicenses() {
        return licenseRepository.findAll();
    }

    public License getLicenseById(Long id) {
        return licenseRepository.findById(id).orElse(null);
    }

    public License createLicense(License license) {
        return licenseRepository.save(license);
    }

    public License deleteLicense(Long id) {
        License license = licenseRepository.findById(id).orElse(null);
        if (license != null) {
            licenseRepository.delete(license);
        }
        return license;
    }

    public License updateLicense(Long id, License license) {
        License updatedLicense = licenseRepository.findById(id).get();
        updatedLicense.setName(license.getName());
        updatedLicense.setWorkers(license.getWorkers());
        return licenseRepository.save(updatedLicense);
    }
}
