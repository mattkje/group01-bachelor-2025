package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.LicenseController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.License;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.LicenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(LicenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class LicenseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LicenseService licenseService;

  @Test
  void testGetAllLicenses() throws Exception {
    // Arrange
    License license1 = new License();
    License license2 = new License();
    when(licenseService.getAllLicenses()).thenReturn(List.of(license1, license2));

    // Act & Assert
    mockMvc.perform(get("/api/licenses"))
        .andExpect(status().isOk());
    verify(licenseService, times(1)).getAllLicenses();
  }

  @Test
  void testGetLicenseById() throws Exception {
    // Arrange
    License license = new License();
    when(licenseService.getLicenseById(1L)).thenReturn(license);

    // Act & Assert
    mockMvc.perform(get("/api/licenses/{id}", 1L))
        .andExpect(status().isOk());
    verify(licenseService, times(1)).getLicenseById(1L);
  }

  @Test
  void testCreateLicense() throws Exception {
    // Arrange
    License license = new License();
    when(licenseService.createLicense(any(License.class))).thenReturn(license);

    // Act & Assert
    mockMvc.perform(post("/api/licenses")
            .contentType("application/json")
            .content("{}")) // To test, you have to replace it with JSON for License
        .andExpect(status().isOk());
    verify(licenseService, times(1)).createLicense(any(License.class));
  }

  @Test
  void testUpdateLicense() throws Exception {
    // Arrange
    License license = new License();
    when(licenseService.updateLicense(eq(1L), any(License.class))).thenReturn(license);

    // Act & Assert
    mockMvc.perform(put("/api/licenses/{id}", 1L)
            .contentType("application/json")
            .content("{}")) // To test, you have to replace it with JSON for License
        .andExpect(status().isOk());
    verify(licenseService, times(1)).updateLicense(eq(1L), any(License.class));
  }

  @Test
  void testDeleteLicense() throws Exception {
    // Arrange
    License license = new License();
    when(licenseService.deleteLicense(1L)).thenReturn(license);

    // Act & Assert
    mockMvc.perform(delete("/api/licenses/{id}", 1L))
        .andExpect(status().isOk());
    verify(licenseService, times(1)).deleteLicense(1L);
  }
}