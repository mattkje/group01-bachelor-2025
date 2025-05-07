package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.serviceTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.LicenseRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

class LicenseServiceTest {

    @Mock
    private LicenseRepository licenseRepository;

    @InjectMocks
    private LicenseService licenseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllLicenses() {
        // Arrange
        License license1 = new License();
        License license2 = new License();
        when(licenseRepository.findAll()).thenReturn(List.of(license1, license2));

        // Act
        List<License> result = licenseService.getAllLicenses();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(licenseRepository, times(1)).findAll();
    }

    @Test
    void testGetLicenseById() {
        // Arrange
        License license = new License();
        when(licenseRepository.findById(1L)).thenReturn(Optional.of(license));

        // Act
        License result = licenseService.getLicenseById(1L);

        // Assert
        assertNotNull(result);
        verify(licenseRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateLicense() {
        // Arrange
        License license = new License();
        when(licenseRepository.save(license)).thenReturn(license);

        // Act
        License result = licenseService.createLicense(license);

        // Assert
        assertNotNull(result);
        verify(licenseRepository, times(1)).save(license);
    }

    @Test
    void testDeleteLicense() {
        // Arrange
        License license = new License();
        when(licenseRepository.findById(1L)).thenReturn(Optional.of(license));

        // Act
        License result = licenseService.deleteLicense(1L);

        // Assert
        assertNotNull(result);
        verify(licenseRepository, times(1)).findById(1L);
        verify(licenseRepository, times(1)).delete(license);
    }

    @Test
    void testUpdateLicense() {
        // Arrange
        License existingLicense = new License();
        License updatedLicense = new License();
        updatedLicense.setName("Updated Name");
        when(licenseRepository.findById(1L)).thenReturn(Optional.of(existingLicense));
        when(licenseRepository.save(existingLicense)).thenReturn(existingLicense);

        // Act
        License result = licenseService.updateLicense(1L, updatedLicense);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", existingLicense.getName());
        verify(licenseRepository, times(1)).findById(1L);
        verify(licenseRepository, times(1)).save(existingLicense);
    }
}